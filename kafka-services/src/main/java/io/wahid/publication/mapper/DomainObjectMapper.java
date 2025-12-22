package io.wahid.publication.mapper;

public interface DomainObjectMapper<S, T, P> {
    T toEntity(S dto);
    S toDto(T entity);
    P toEvent(S dto);
    T toEntityFromEvent(P event);
}