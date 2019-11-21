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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ContextConfiguration;
import org.testng.Assert;
import org.testng.annotations.Test;


/**
 * Test matching methods of PdfCreationFilter.
 *
 * @author Martin Goellnitz, Markus Schwarz
 */
@ContextConfiguration(classes = PdfFilterConfiguration.class)
public class PdfCreationFilterTest extends org.springframework.test.context.testng.AbstractTestNGSpringContextTests {
    
    private static final String BASE_URL = "https://www.example.com/blueprint/servlet/de";

    @Autowired
    private PdfCreationFilter pdfCreationFilter;

    @Test
    public void testPatternNotMatching() {
        String uri = BASE_URL + "/title?view=fragment&p13n_test=true&p13n_testcontext=0";
        MockHttpServletRequest request = new MockHttpServletRequest("GET", uri);
        Assert.assertFalse(pdfCreationFilter.matches(request));
    }


    @Test
    public void testPatternMatching() {
        String uri = BASE_URL + "/title?template=pdf&p13n_test=true&p13n_testcontext=0";
        MockHttpServletRequest request = new MockHttpServletRequest("GET", uri);
        Assert.assertTrue(pdfCreationFilter.matches(request));
    }


    @Test
    public void testPatternNotViewParameter() {
        String uri = BASE_URL + "/template=pdf/title?view=fragmentPreview";
        MockHttpServletRequest request = new MockHttpServletRequest("GET", uri);
        Assert.assertFalse(pdfCreationFilter.matches(request));
    }


    @Test
    public void testPatternNotFirstParameter() {
        String uri = BASE_URL + "/template=pdf/title?a=b&template=pdf";
        MockHttpServletRequest request = new MockHttpServletRequest("GET", uri);
        Assert.assertFalse(pdfCreationFilter.matches(request));
    }

}
