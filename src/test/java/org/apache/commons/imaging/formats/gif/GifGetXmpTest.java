/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.imaging.formats.gif;

import org.apache.commons.imaging.ImagingException;
import org.apache.commons.imaging.bytesource.ByteSource;
import org.apache.commons.imaging.common.XmpImagingParameters;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class GifGetXmpTest {

    @Test
    public void testXmpBlockMissingMagicTrailer() throws IOException {
        // Create a mock ByteSource that returns a GIF block without the required magic trailer
        ByteSource mockByteSource = ByteSource.array(new byte[]{'G', 'I', 'F', '8', '9', 'a',  // GIF header
                // Fake XMP block header
                'X', 'M', 'P', ' ', 'D', 'a', 't', 'a', '1', '2', '3', '4',
                // Missing magic trailer at the end
        });

        assertThrows(ImagingException.class, () -> {
            new GifImageParser().getXmpXml(mockByteSource, new XmpImagingParameters<>());
        });
    }
}