package de.provocon.coremedia.cms.view;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * The entry point of our module, referenced by META-INF/spring.factories.
 *
 * The component scan below  takes care of instantiating {@link PdfCreationFilter}
 *
 * @author Markus Schwarz
 */
@Configuration
@ComponentScan(basePackages = "de.provocon.coremedia.cms.view")
public class PdfFilterConfiguration {
}
