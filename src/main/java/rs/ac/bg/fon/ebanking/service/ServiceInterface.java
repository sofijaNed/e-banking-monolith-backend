package rs.ac.bg.fon.ebanking.service;

import java.util.List;

public interface ServiceInterface<T> {
    List<T> findAll();


    T findById(Object id) throws Exception;


    T save(T t) throws Exception;


    T update(T t) throws Exception;


}
