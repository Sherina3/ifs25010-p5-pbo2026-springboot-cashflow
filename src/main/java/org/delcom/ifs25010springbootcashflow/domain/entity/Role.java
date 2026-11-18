package org.delcom.ifs25010springbootcashflow.domain.entity;

/** Peran pengguna aplikasi. */
public enum Role {
    /** Administrator: melihat daftar user dan transaksi user lain (read-only). */
    ADMIN,

    /** Pengguna biasa: hanya mengelola data miliknya sendiri. */
    USER
}
