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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;


/**
 * Wrap a Servlet Response for post processing with this wrapper.
 *
 * @author Martin Goellnitz
 */
public final class PdfResponseWrapper extends HttpServletResponseWrapper {

    /**
     * The underlaying stream to buffer data.
     */
    private final ByteArrayOutputStream underlaying;

    /**
     * Writer derivative of the underlaying.
     */
    private PrintWriter streamWriter = null;

    /**
     * Internal status cannot be passed to the superclass due to potentially impossible changes there.
     */
    private int status = SC_OK;


    /**
     * Create a new wrapper instance.
     * Only allow creation of this wrapper for this package.
     *
     * @param httpServletResponse servlet response to be wrapped
     */
    protected PdfResponseWrapper(HttpServletResponse httpServletResponse) {
        super(httpServletResponse);
        underlaying = new ByteArrayOutputStream();
    }


    /**
     * Get underlaying stream.
     * Only used internally to pass the buffer to the outside users.
     *
     * @return underlaying byte array output stream
     */
    protected ByteArrayOutputStream getUnderlaying() {
        return underlaying;
    }


    @Override
    public PrintWriter getWriter() throws IOException {
        if (streamWriter==null) {
            streamWriter = new PrintWriter(underlaying);
        }
        return streamWriter;
    }


    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return new ServletOutputStream() {

            private ByteArrayOutputStream baostr = getUnderlaying();


            @Override
            public void write(int b) throws IOException {
                baostr.write(b);
            }
        };
    }


    @Override
    public void setStatus(int i) {
        status = i;
        super.setStatus(i);
    }


    @Override
    public void sendError(int i, String string) throws IOException {
        status = i;
        super.sendError(i, string);
    }


    @Override
    public void sendError(int i) throws IOException {
        status = i;
        super.sendError(i);
    }


    @Override
    @Deprecated
    public void setStatus(int i, String string) {
        status = i;
    }


    @Override
    public int getStatus() {
        return status;
    }


    /**
     * Get the content as a string.
     *
     * @return string in default encoding.
     */
    protected String getContent() {
        if (streamWriter!=null) {
            streamWriter.flush();
        }
        return underlaying.toString();
    }


    /**
     * Get content of internal buffer.
     *
     * @return content of internal buffer as a byte array.
     */
    protected byte[] getBytes() {
        if (streamWriter!=null) {
            streamWriter.flush();
        }
        return underlaying.toByteArray();
    }


    /**
     * Get content of internal buffer
     *
     * @return content of internal buffer as a fresh input stream instance.
     */
    protected InputStream getStream() {
        if (streamWriter!=null) {
            streamWriter.flush();
        }
        return new ByteArrayInputStream(underlaying.toByteArray());
    }

}
