package com.example.trixi.service;

import com.example.trixi.model.CastObce;
import com.example.trixi.model.Obec;
import com.example.trixi.repository.DataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class XmlParsingService {

    private static final Logger log = LoggerFactory.getLogger(XmlParsingService.class);
    private static final String VF_NAMESPACE = "urn:cz:isvs:ruian:schemas:VymennyFormatTypy:v1";
    
    private final DataRepository dataRepository;

    public XmlParsingService(DataRepository dataRepository) {
        this.dataRepository = dataRepository;
    }

    public void processFromUrl(String url) throws Exception {
        log.info("Starting XML processing from: {}", url);
        try (InputStream in = URI.create(url).toURL().openStream();
             ZipInputStream zis = new ZipInputStream(in)) {

            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().endsWith(".xml")) {
                    log.info("Processing entry: {}", entry.getName());
                    parseAndSave(zis);
                }
            }
        }
        log.info("Finished XML processing.");
    }

    void parseAndSave(InputStream xmlStream) throws Exception {
        Document doc = parseDocument(xmlStream);
        if (doc == null) return;

        NodeList dataNodes = doc.getDocumentElement().getElementsByTagNameNS(VF_NAMESPACE, "Data");
        if (dataNodes.getLength() == 0) {
            log.warn("No 'Data' element found in XML.");
            return;
        }
        Element dataElem = (Element) dataNodes.item(0);

        List<String> failedRecords = new ArrayList<>();
        int countObce = processObce(dataElem, failedRecords);
        int countCasti = processCastiObci(dataElem, failedRecords);

        if (!failedRecords.isEmpty()) {
            log.warn("Finished with {} errors: {}", failedRecords.size(), String.join("; ", failedRecords));
        }
        log.info("Successfully processed {} Obec and {} CastiObce.", countObce, countCasti);
    }

    private Document parseDocument(InputStream xmlStream) throws Exception {
        byte[] bytes = xmlStream.readAllBytes();
        if (bytes.length == 0) {
            log.warn("Empty XML stream encountered.");
            return null;
        }

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);

        try (InputStream nonClosingStream = new ByteArrayInputStream(bytes)) {
            return factory.newDocumentBuilder().parse(nonClosingStream);
        } catch (Exception e) {
            log.error("Failed to parse XML: {}", e.getMessage());
            return null;
        }
    }

    private int processObce(Element dataElem, List<String> failedRecords) {
        int count = 0;
        NodeList obceContainers = dataElem.getElementsByTagNameNS(VF_NAMESPACE, "Obce");
        if (obceContainers.getLength() == 0) return 0;

        Element obceContainer = (Element) obceContainers.item(0);
        NodeList obceList = obceContainer.getElementsByTagNameNS(VF_NAMESPACE, "Obec");
        for (int i = 0; i < obceList.getLength(); i++) {
            Element obecElem = (Element) obceList.item(i);
            Integer kod = getChildTextAsInteger(obecElem, "Kod");
            String nazev = getChildText(obecElem, "Nazev");
            if (kod != null && nazev != null) {
                dataRepository.saveObec(new Obec(kod, nazev));
                count++;
            } else {
                failedRecords.add(String.format("Missing data for Obec (Kod: %s, Nazev: %s)", kod, nazev));
            }
        }
        return count;
    }

    private int processCastiObci(Element dataElem, List<String> failedRecords) {
        int count = 0;
        NodeList castiContainers = dataElem.getElementsByTagNameNS(VF_NAMESPACE, "CastiObci");
        if (castiContainers.getLength() == 0) return 0;

        Element castiContainer = (Element) castiContainers.item(0);
        NodeList castiList = castiContainer.getElementsByTagNameNS(VF_NAMESPACE, "CastObce");
        for (int i = 0; i < castiList.getLength(); i++) {
            Element castElem = (Element) castiList.item(i);
            Integer kod = getChildTextAsInteger(castElem, "Kod");
            String nazev = getChildText(castElem, "Nazev");

            NodeList innerObecNodes = castElem.getElementsByTagNameNS("*", "Obec");
            Element innerObec = (Element) innerObecNodes.item(0);
            Integer obecKod = (innerObec != null) ? getChildTextAsInteger(innerObec, "Kod") : null;

            if (kod != null && nazev != null && obecKod != null) {
                dataRepository.saveCastObce(new CastObce(kod, nazev, obecKod));
                count++;
            } else {
                failedRecords.add(String.format("Missing data for CastObce (Kod: %s, Nazev: %s, ObecKod: %s)", kod, nazev, obecKod));
            }
        }
        return count;
    }

    private String getChildText(Element parent, String localName) {
        NodeList children = parent.getElementsByTagNameNS("*", localName);
        if (children.getLength() > 0) {
            return children.item(0).getTextContent().trim();
        }
        return null;
    }

    private Integer getChildTextAsInteger(Element parent, String localName) {
        String text = getChildText(parent, localName);
        if (text != null && !text.isEmpty()) {
            try {
                return Integer.parseInt(text);
            } catch (NumberFormatException ignored) {}
        }
        return null;
    }
}
