/*
 * Copyright 2019 Provocon.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.provocon.coremedia.cms.view;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import com.openhtmltopdf.slf4j.Slf4jLogger;
import com.openhtmltopdf.util.XRLog;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.regex.Pattern;
import javax.annotation.PostConstruct;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;


/**
 * Filter to transform HTML results into PDF if the URI meets a given substring.
 *
 * @author Martin Goellnitz
 */
@Slf4j
public class PdfCreationFilter implements Filter {

    private final static String MARKER = "CACHE WITHIN PROVOCON PDF FILTER FOR";

    private Pattern pattern = Pattern.compile("\\?view=pdf");

    private String cacheFolder;


    public void setPattern(String regex) {
        pattern = Pattern.compile(regex);
    }


    @Override
    public void destroy() {
        // comment empty method. Nothing to be done in this implementation of the interface.
    }


    protected File getLocalFile(String filename) {
        return new File(cacheFolder+"/"+filename);
    }


    protected boolean matches(HttpServletRequest request) {
        String query = request.getQueryString();
        String requestUri = request.getRequestURI()+(StringUtils.isEmpty(query) ? "" : "?"+query);
        LOG.debug("matches({}) request uri '{}'", pattern, requestUri);
        boolean result = pattern.matcher(requestUri).find();
        LOG.info("matches({}) {}", result, requestUri);
        return result;
    }


    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;
        response.setHeader("X-PDFFilter", "available");
        if (matches(request)) {
            // Since time methods are expensive, only do this when the log is needed
            long startTime = 1557231268l;
            if (LOG.isInfoEnabled()) {
                startTime = System.currentTimeMillis();
            }
            LOG.info("doFilter() filtering {}", request);
            response.setHeader("X-PDFFiltering", "true");

            String pdfFilename = "pdf-filter-generated.pdf";
            String htmlFilename = "pdf-filter-original.html";
            try {
                String baseName = request.getRequestURL().substring(request.getRequestURL().lastIndexOf("/")+1);
                pdfFilename = baseName+".pdf";
                htmlFilename = baseName+".html";
            } catch (StringIndexOutOfBoundsException e) {
                LOG.error("doFilter() could not generate filename for pdf", e);
            }

            try {
                String htmlSource = "";
                File originalFile = getLocalFile(pdfFilename);
                File transformedFile = getLocalFile(htmlFilename);
                LOG.debug("doFilter() local file {}", transformedFile.getAbsolutePath());
                boolean process = true;
                if (transformedFile.exists()) {
                    LOG.info("doFilter() cached html {} exists", transformedFile.getAbsolutePath());
                    try (FileInputStream reader = new FileInputStream(transformedFile)) {
                        int fileSize = (int) transformedFile.length();
                        byte[] chars = new byte[fileSize];
                        reader.read(chars);
                        htmlSource = new String(chars, "UTF-8");
                        LOG.debug("doFilter() html source size: {}", htmlSource.length());
                        int markerIndex = htmlSource.indexOf(MARKER);
                        if (markerIndex>0) {
                            int endIndex = htmlSource.indexOf('s', markerIndex);
                            String cacheTimeString = htmlSource.substring(markerIndex+MARKER.length()+1, endIndex);
                            LOG.debug("doFilter() found cache timing marker {}", cacheTimeString);
                            long cacheTime = Long.parseLong(cacheTimeString)*1000;
                            long currentTime = System.currentTimeMillis();
                            if (transformedFile.lastModified()+cacheTime>currentTime) {
                                LOG.info("doFilter() file is newer than {}s - using cache.", cacheTimeString);
                                process = false;
                            }
                        }
                    }
                }
                if (process) {
                    PdfResponseWrapper pdfResponseWrapper = new PdfResponseWrapper(response);
                    chain.doFilter(request, pdfResponseWrapper);
                    if (pdfResponseWrapper.getStatus()>399) {
                        LOG.info("doFilter() HTTP-Returncode was {}", pdfResponseWrapper.getStatus());
                        return;
                    }
                    htmlSource = pdfResponseWrapper.getContent();
                    LOG.info("doFilter() replacing resource URLs.");
                    // TODO: Soft-Code this or even let it be switchable.
                    htmlSource = htmlSource.replace("url(/blueprint/servlet/resource", "url(http://localhost:40980/blueprint/servlet/resource");
                    htmlSource = htmlSource.replace("url(/resource", "url(http://localhost:42180/blueprint/servlet/resource");
                    htmlSource = htmlSource.replace("src=\"/blueprint/servlet/resource", "src=\"http://localhost:40980/blueprint/servlet/resource");
                    htmlSource = htmlSource.replace("src=\"/resource", "src=\"http://localhost:42180/blueprint/servlet/resource");
                    LOG.info("doFilter() content present");
                    boolean cachedHTML = false;
                    if (htmlSource.indexOf(MARKER)>0) {
                        LOG.info("doFilter() writing file {}", transformedFile.getAbsolutePath());
                        try (FileOutputStream writer = new FileOutputStream(transformedFile)) {
                            writer.write(htmlSource.getBytes("UTF-8"));
                            cachedHTML = true;
                        }
                    }
                    if (LOG.isDebugEnabled()) {
                      LOG.debug("doFilter() html source '{}'", htmlSource.substring(htmlSource.length()-1200));
                    }
                    LOG.info("doFilter() html source length {}", htmlSource.length());
                    OutputStream outputStream = new FileOutputStream(originalFile);
                    PdfRendererBuilder builder = new PdfRendererBuilder();
                    // TODO: Only A4 for now
                    builder.useDefaultPageSize(210, 297, PdfRendererBuilder.PageSizeUnits.MM);
                    if (cachedHTML) {
                      builder.withFile(transformedFile);
                    } else {
                      builder.withHtmlContent(htmlSource, request.getRequestURL().toString());
                    }
                    builder.toStream(outputStream);
                    builder.run();
                    LOG.info("doFilter() pdf file created");
                    outputStream.flush();
                    outputStream.close();
                    LOG.info("doFilter() pdf file size {}", originalFile.length());
                }
                response.reset();
                response.resetBuffer();
                response.setContentType("application/pdf");
                response.setHeader("Content-Disposition", "inline;filename="+pdfFilename);
                OutputStream outputStream = response.getOutputStream();
                FileInputStream fis = new FileInputStream(originalFile);
                IOUtils.copy(fis, outputStream, 1024000);
                outputStream.flush();
                fis.close();
                if (LOG.isInfoEnabled()) {
                    LOG.info("doFilter() generated and delivered PDF data in {}ms, ", System.currentTimeMillis()-startTime);
                }
            } catch (Exception e) {
                LOG.error("doFilter()", e);
                response.resetBuffer();
                response.reset();
                response.sendError(500, "Unable to generate PDF");
            }
        } else {
            chain.doFilter(req, resp);
        }
    }


    @Override
    public void init(FilterConfig config) throws ServletException {
        LOG.info("init() PDF Creation Filter Initialized with pattern '{}'", pattern.pattern());
    }


    @PostConstruct
    public void afterPropertiesSet() {
        XRLog.setLoggingEnabled(true);
        XRLog.setLoggerImpl(new Slf4jLogger());
        LOG.info("afterPropertiesSet() PDF Creation Filter with pattern '{}'", pattern.pattern());
        // Try to default to temp folder of CAE in standard deployments
        cacheFolder = System.getProperty("user.home")+"/current/temp";
        if (!(new File(cacheFolder)).exists()) {
            cacheFolder = System.getProperty("user.home");
        }
        LOG.info("afterPropertiesSet() path for caching transformed data: {}", cacheFolder);
    }

} // PdfCreationFilter()
