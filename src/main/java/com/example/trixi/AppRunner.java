package com.example.trixi;

import com.example.trixi.repository.DataRepository;
import com.example.trixi.service.XmlParsingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
public class AppRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AppRunner.class);

    private final XmlParsingService xmlParsingService;
    private final DataRepository dataRepository;

    public AppRunner(XmlParsingService xmlParsingService, DataRepository dataRepository) {
        this.xmlParsingService = xmlParsingService;
        this.dataRepository = dataRepository;
    }

    @Override
    public void run(String... args) {
        try {
            log.info("AppRunner started. Cleaning up database...");
            dataRepository.deleteAll();
            String url = "https://www.smartform.cz/download/kopidlno.xml.zip";
            xmlParsingService.processFromUrl(url);
            log.info("AppRunner finished successfully.");
        } catch (Exception e) {
            log.error("AppRunner failed with error: {}", e.getMessage(), e);
        }
    }
}
