package com.example.trixi.service;

import com.example.trixi.model.CastObce;
import com.example.trixi.model.Obec;
import com.example.trixi.repository.DataRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class XmlParsingServiceTest {

    @Test
    void testParseAndSave() throws Exception {
        DataRepository dataRepository = mock(DataRepository.class);
        XmlParsingService xmlParsingService = new XmlParsingService(dataRepository);

        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<vf:VymennyFormat xmlns:vf=\"urn:cz:isvs:ruian:schemas:VymennyFormatTypy:v1\"\n" +
                "                  xmlns:obi=\"urn:cz:isvs:ruian:schemas:ObecIntTypy:v1\"\n" +
                "                  xmlns:coi=\"urn:cz:isvs:ruian:schemas:CastObceIntTypy:v1\">\n" +
                "  <vf:Hlavicka>\n" +
                "    <vf:Obce>\n" +
                "      <vf:Obec>\n" +
                "        <obi:Kod>123</obi:Kod>\n" +
                "        <obi:Nazev>IgnorovatVHlavicce</obi:Nazev>\n" +
                "      </vf:Obec>\n" +
                "    </vf:Obce>\n" +
                "  </vf:Hlavicka>\n" +
                "  <vf:Data>\n" +
                "    <vf:Obec>\n" +
                "      <obi:Kod>456</obi:Kod>\n" +
                "      <obi:Nazev>IgnorovatMimoObce</obi:Nazev>\n" +
                "    </vf:Obec>\n" +
                "    <vf:Obce>\n" +
                "      <vf:Obec>\n" +
                "        <obi:Kod>573060</obi:Kod>\n" +
                "        <obi:Nazev>Kopidlno</obi:Nazev>\n" +
                "      </vf:Obec>\n" +
                "    </vf:Obce>\n" +
                "    <vf:CastiObci>\n" +
                "      <vf:CastObce>\n" +
                "        <coi:Kod>69299</coi:Kod>\n" +
                "        <coi:Nazev>Kopidlno</coi:Nazev>\n" +
                "        <coi:Obec>\n" +
                "          <obi:Kod>573060</obi:Kod>\n" +
                "        </coi:Obec>\n" +
                "      </vf:CastObce>\n" +
                "      <vf:CastObce>\n" +
                "         <coi:Kod>31801</coi:Kod>\n" +
                "         <coi:Nazev>Drahoraz</coi:Nazev>\n" +
                "         <coi:Obec>\n" +
                "            <obi:Kod>573060</obi:Kod>\n" +
                "         </coi:Obec>\n" +
                "      </vf:CastObce>\n" +
                "    </vf:CastiObci>\n" +
                "  </vf:Data>\n" +
                "</vf:VymennyFormat>";

        xmlParsingService.parseAndSave(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));

        verify(dataRepository).saveObec(any(Obec.class));
        verify(dataRepository, times(2)).saveCastObce(any(CastObce.class));

        ArgumentCaptor<Obec> obecCaptor = ArgumentCaptor.forClass(Obec.class);
        verify(dataRepository).saveObec(obecCaptor.capture());
        assertEquals(573060, obecCaptor.getValue().kod());
        assertEquals("Kopidlno", obecCaptor.getValue().nazev());

        ArgumentCaptor<CastObce> castiCaptor = ArgumentCaptor.forClass(CastObce.class);
        verify(dataRepository, times(2)).saveCastObce(castiCaptor.capture());
        List<CastObce> casti = castiCaptor.getAllValues();
        assertEquals(69299, casti.get(0).kod());
        assertEquals(31801, casti.get(1).kod());
        assertEquals("Drahoraz", casti.get(1).nazev());
        assertEquals(573060, casti.get(1).obecKod());
    }
}
