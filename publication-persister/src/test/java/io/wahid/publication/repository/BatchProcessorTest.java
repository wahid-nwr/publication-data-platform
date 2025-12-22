package io.wahid.publication.repository;

import io.wahid.publication.dto.AuthorDto;
import io.wahid.publication.exception.BatchProcessingException;
import org.hibernate.service.spi.ServiceException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.Mockito;

import java.util.concurrent.ExecutionException;
import java.util.stream.Stream;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class BatchProcessorTest {

    @Test
    void shouldHandleInterruptedException() throws InterruptedException, ExecutionException {
//        InterruptedException ex = mock(InterruptedException.class);
//        BatchProcessor<AuthorDto> mockBatchProcessor = Mockito.mock(BatchProcessor.class);
//        doThrow(new BatchProcessingException("Mock interruption", ex))
//                .when(mockBatchProcessor).persist(any());
//
//        ServiceException ex2 = mock(ServiceException.class);
//        BatchProcessor<AuthorDto> mockBatchProcessor2 = Mockito.mock(BatchProcessor.class);
//        doThrow(new BatchProcessingException(ex2))
//                .when(mockBatchProcessor2).persist(any());
//
//        AuthorDto data1 = new AuthorDto();
//        AuthorDto data2 = new AuthorDto();
//        Stream<AuthorDto> input = Stream.of(data1, data2);
//        assertThrows(BatchProcessingException.class, () -> mockBatchProcessor.persist(input));
//        assertThrows(BatchProcessingException.class, () -> mockBatchProcessor2.persist(input));
    }
}
