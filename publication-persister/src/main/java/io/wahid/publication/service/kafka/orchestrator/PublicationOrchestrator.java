package io.wahid.publication.service.kafka.orchestrator;

import io.wahid.publication.events.EventPublisher;
import io.wahid.publication.events.KafkaEventPublisher;
import io.wahid.publication.events.event.ProcessCompleteEvent;
import io.wahid.publication.model.Author;
import io.wahid.publication.model.events.PendingPublication;
import io.wahid.publication.model.events.Status;
import io.wahid.publication.repository.AuthorRepository;
import io.wahid.publication.repository.events.PendingPublicationRepository;
import io.wahid.publication.util.JpaUtil;
import io.wahid.publication.util.KafkaUtil;
import com.publication.events.AuthorReadyEvent;
import com.publication.events.PublicationCreatedEvent;
import com.publication.events.PublicationReadyEvent;
import jakarta.persistence.EntityManagerFactory;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.KafkaProducer;

import javax.annotation.Nonnull;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static io.wahid.publication.KafkaConstants.*;

public class PublicationOrchestrator {
    private static final Logger LOGGER = Logger.getLogger(PublicationOrchestrator.class.getName());

    private PendingPublicationRepository repo;
    private AuthorRepository authorRepo;
    private final EventPublisher publisher;

    public PublicationOrchestrator() {
        LOGGER.log(Level.INFO,"🪲 PublicationOrchestrator starting. Initializing kafka publisher ...");
        Properties props = KafkaUtil.getProducerProperties(BOOTSTRAP_SERVER);
        KafkaProducer<String, SpecificRecordBase> avroProducer = new KafkaProducer<>(props);
        this.publisher = new KafkaEventPublisher(null, avroProducer);
        LOGGER.log(Level.INFO,"✨ PublicationOrchestrator started. will listen to Kafka topics...");
    }

    public void handleEvent(ConsumerRecord<String, SpecificRecordBase> consumerRecord) {
        ensureRepos();
        String topic = consumerRecord.topic();
        try {
            switch (topic) {
                case AUTHOR_TOPIC:
                    AuthorReadyEvent authorEvent = (AuthorReadyEvent) consumerRecord.value();
                    handleAuthorCreated(authorEvent);
                    break;

                case PENDING_PUBLICATION_CREATED_TOPIC:
                    PublicationCreatedEvent publicationCreatedEvent = (PublicationCreatedEvent) consumerRecord.value();
                    if (Status.PENDING.name().contentEquals(publicationCreatedEvent.getPendingPublication().getStatus())) {
                        onPublicationCreated(publicationCreatedEvent);
                    }
                    break;

                case NO_PENDING:
                    publisher.publish(PROCESS_COMPLETE, new ProcessCompleteEvent());
                    break;

                default:
                    LOGGER.log(Level.INFO,"⚠️ Unknown topic: {0}",  topic);
            }
        } catch (Exception e) {
            Logger.getGlobal().log(Level.SEVERE, e.getMessage(), e);
        }
    }

    public void tryMakePublicationReady(PendingPublication pub) {
        ensureRepos();
        PendingPublication persisted = repo.findByIsbn(pub.getIsbn());
        if (persisted == null || Status.READY.equals(persisted.getStatus())) {
            return;
        }
        Set<String> existingAuthors = authorRepo
                .findByEmail(pub.getAuthorEmails())
                .stream()
                .map(Author::getEmail)
                .collect(Collectors.toSet());

        if (existingAuthors.containsAll(pub.getAuthorEmails())) {
            PublicationReadyEvent ready = getPublicationReadyEvent(pub);

            publisher.publish(PUBLICATION_READY_TOPIC, ready);
            pub.setStatus(Status.READY);
            repo.updateStatus(pub.getIsbn(), Status.READY);
            LOGGER.log(Level.INFO,"✅ Publication ready -> {0}", pub.getTitle());
        }
    }

    private synchronized void ensureRepos() {
        if (repo != null) {
            return;
        }
        LOGGER.log(Level.INFO,"🪲 PublicationOrchestrator started. Ensuring repo initializing...");
        EntityManagerFactory emf = JpaUtil.getEntityManagerFactory();
        this.repo = new PendingPublicationRepository(emf);
        this.authorRepo = new AuthorRepository(emf);
        LOGGER.log(Level.INFO,"🪲 Repository initialized!!");
    }

    private void handleAuthorCreated(AuthorReadyEvent event) {
        // author persisted, now check pending publications
        List<PendingPublication> candidates = repo.findByAuthor(event.getAuthor().getEmail().toString());
        for (PendingPublication pub : candidates) {
            if (allAuthorsExist(pub.getAuthorEmails())) {
                finalizePublication(pub);
            }
        }
    }

    private PendingPublication getPendingPublicationFromEvent(@Nonnull com.publication.events.PendingPublication pub) {
        List<String> emails =  pub.getAuthorEmails().stream().map(CharSequence::toString).toList();
        String id = String.valueOf(pub.getId());
        if (pub.getType().toString().equals("BOOK")) {
            return new PendingPublication(id, pub.getTitle().toString(),
                    pub.getIsbn().toString(), emails, pub.getDescription().toString(), Status.valueOf(pub.getStatus().toString()));
        } else if (pub.getType().toString().equals("MAGAZINE")) {
            List<Integer> dateList = pub.getPublicationDate().stream().map(Number::intValue).toList();
            LocalDate date = LocalDate.of(dateList.get(0), dateList.get(1), dateList.get(2));
            return new PendingPublication(id, pub.getTitle().toString(),
                    pub.getIsbn().toString(), emails, date, Status.valueOf(pub.getStatus().toString()));
        }
        return null;
    }

    private void onPublicationCreated(PublicationCreatedEvent event) throws Exception {
        com.publication.events.PendingPublication pub = event.getPendingPublication();
        tryMakePublicationReady(Objects.requireNonNull(getPendingPublicationFromEvent(pub)));
    }

    private boolean allAuthorsExist(List<String> authorEmails) {
        return authorRepo.existsAll(authorEmails);
    }

    private void finalizePublication(PendingPublication pub) {
        PublicationReadyEvent ready = new PublicationReadyEvent();
        ready.setPublicationId(pub.getId());
        ready.setType(pub.getType().toString());
        ready.setTitle(pub.getTitle());
        ready.setAuthorEmails(new ArrayList<>(pub.getAuthorEmails()));
        ready.setDescription(pub.getDescription());
        LocalDate date = pub.getPublicationDate() != null ? pub.getPublicationDate() : LocalDate.now();
        ready.setPublicationDate(List.of(date.getYear(), date.getMonthValue(), date.getDayOfMonth()));
        ready.setIsbn(pub.getIsbn());
        ready.setEventType("PublicationReady");
        ready.setOccurredAt(Instant.now().toEpochMilli());

        publisher.publish(PUBLICATION_READY_TOPIC, ready);
        pub.setStatus(Status.READY);
        repo.updateStatus(pub.getIsbn(), Status.READY);
    }

    private PublicationReadyEvent getPublicationReadyEvent(PendingPublication pub) {
        PublicationReadyEvent ready = new PublicationReadyEvent();
        ready.setPublicationId(pub.getId());
        ready.setType(pub.getType().toString());
        ready.setTitle(pub.getTitle());
        ready.setAuthorEmails(new ArrayList<>(pub.getAuthorEmails()));
        ready.setDescription(pub.getDescription());

        LocalDate date = pub.getPublicationDate() != null ? pub.getPublicationDate() : LocalDate.now();
        ready.setPublicationDate(List.of(date.getYear(), date.getMonthValue(), date.getDayOfMonth()));
        ready.setIsbn(pub.getIsbn());
        ready.setEventType("PublicationReady");
        ready.setOccurredAt(ready.getOccurredAt());
        return ready;
    }
}
