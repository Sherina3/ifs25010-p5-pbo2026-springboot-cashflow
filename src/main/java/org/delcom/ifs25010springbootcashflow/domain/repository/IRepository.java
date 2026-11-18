package org.delcom.ifs25010springbootcashflow.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Port generik CRUD + pencarian.
 *
 * @param <T>  tipe entitas domain
 * @param <ID> tipe identifier entitas
 */
public interface IRepository<T, ID> {
    List<T> findAll();

    Optional<T> findById(ID id);

    /** Menyimpan entitas baru dan mengembalikannya (lengkap dengan ID yang dibangkitkan). */
    T save(T entity);

    /** Menghapus berdasarkan ID; true jika ada data yang terhapus. */
    boolean deleteById(ID id);

    /** Menyimpan perubahan entitas yang sudah ada. */
    void update(T entity);

    List<T> findBy(Predicate<T> predicate);
}
