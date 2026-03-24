package com.example.trixi.service;

import com.example.trixi.model.CastObce;
import com.example.trixi.model.Obec;
import com.example.trixi.repository.DataRepository;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
class DataRepoTest {

    @Autowired
    private DataRepository dataRepository;

    @Autowired
    private DSLContext dsl;

    @BeforeEach
    void setUp() {
        dataRepository.deleteAll();
    }

    @Test
    void testSaveData() {
        Obec obec = new Obec(573060, "Kopidlno");
        List<CastObce> casti = List.of(
                new CastObce(69299, "Kopidlno", 573060),
                new CastObce(69302, "Ledkov", 573060)
        );

        dataRepository.saveData(obec, casti);

        int countObec = dsl.fetchCount(DSL.table("obec"));
        assertEquals(1, countObec);

        int countCasti = dsl.fetchCount(DSL.table("cast_obce"));
        assertEquals(2, countCasti);

        String nazevObce = dsl.select(DSL.field("nazev", String.class))
                .from(DSL.table("obec"))
                .where(DSL.field("kod").eq(573060))
                .fetchOneInto(String.class);
        assertEquals("Kopidlno", nazevObce);
    }

    @Test
    void testDeleteAll() {
        Obec obec = new Obec(573060, "Kopidlno");
        dataRepository.saveData(obec, List.of());
        assertEquals(1, dsl.fetchCount(DSL.table("obec")));

        dataRepository.deleteAll();

        assertEquals(0, dsl.fetchCount(DSL.table("obec")));
        assertEquals(0, dsl.fetchCount(DSL.table("cast_obce")));
    }
}
