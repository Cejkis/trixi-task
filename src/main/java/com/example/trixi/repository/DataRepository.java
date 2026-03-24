package com.example.trixi.repository;

import com.example.trixi.model.CastObce;
import com.example.trixi.model.Obec;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DataRepository {

    private final DSLContext dsl;

    public DataRepository(DSLContext dsl) {
        this.dsl = dsl;
    }

    @Transactional
    public void deleteAll() {
        dsl.deleteFrom(DSL.table("cast_obce")).execute();
        dsl.deleteFrom(DSL.table("obec")).execute();
    }

    @Transactional
    public void saveObec(Obec obec) {
        dsl.insertInto(DSL.table("obec"))
                .set(DSL.field("kod", Integer.class), obec.kod())
                .set(DSL.field("nazev", String.class), obec.nazev())
                .onDuplicateKeyUpdate()
                .set(DSL.field("nazev", String.class), obec.nazev())
                .execute();
    }

    @Transactional
    public void saveCastObce(CastObce cast) {
        dsl.insertInto(DSL.table("cast_obce"))
                .set(DSL.field("kod", Integer.class), cast.kod())
                .set(DSL.field("nazev", String.class), cast.nazev())
                .set(DSL.field("obec_kod", Integer.class), cast.obecKod())
                .onDuplicateKeyUpdate()
                .set(DSL.field("nazev", String.class), cast.nazev())
                .set(DSL.field("obec_kod", Integer.class), cast.obecKod())
                .execute();
    }

    @Transactional
    public void saveData(Obec obec, List<CastObce> castiObce) {
        saveObec(obec);
        for (CastObce cast : castiObce) {
            saveCastObce(cast);
        }
    }
}
