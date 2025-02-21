/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.
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

 package org.apache.commons.imaging.formats.tiff;

 import static org.junit.jupiter.api.Assertions.assertThrows;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.when;
 
 import java.io.File;
 import java.nio.ByteOrder;
 
 import org.apache.commons.imaging.ImagingException;
 import org.apache.commons.imaging.ImagingFormatException;
 import org.apache.commons.imaging.bytesource.ByteSource;
 import org.apache.commons.imaging.formats.tiff.constants.TiffTagConstants;
 import org.junit.jupiter.api.Test;
 
 /**
  * Tests for TiffImageParser, including:
  * 1) A regression test for Google oss-fuzz issue 53669.
  * 2) Three tests that exercise branches 3, 4, and 12 in getBufferedImage().
  */
 public class TiffImageParserTest {
 
     /**
      * Regression test for Google oss-fuzz issue 53669.
      * This test should remain as you originally had it.
      */
     @Test
     public void testOssFuzzIssue53669() {
         assertThrows(ImagingFormatException.class,
             () -> new TiffImageParser().getBufferedImage(
                 ByteSource.file(new File(
                     "src/test/resources/images/tiff/oss-fuzz-53669/"
                     + "clusterfuzz-testcase-minimized-ImagingTiffFuzzer-5965016805539840.tiff")),
                 null));
     }
 
     @Test
     public void testGetBufferedImage_zeroWidthRectangle() throws Exception {

         TiffImagingParameters mockedParams = mock(TiffImagingParameters.class);
         when(mockedParams.isSubImageSet()).thenReturn(true);
         when(mockedParams.getSubImageX()).thenReturn(0);
         when(mockedParams.getSubImageY()).thenReturn(0);
         when(mockedParams.getSubImageWidth()).thenReturn(0);   // zero width
         when(mockedParams.getSubImageHeight()).thenReturn(100);
 
         TiffDirectory directory = mock(TiffDirectory.class);
         when(directory.findField(TiffTagConstants.TIFF_TAG_COMPRESSION)).thenReturn(null);
         when(directory.getSingleFieldValue(TiffTagConstants.TIFF_TAG_IMAGE_WIDTH)).thenReturn(100);
         when(directory.getSingleFieldValue(TiffTagConstants.TIFF_TAG_IMAGE_LENGTH)).thenReturn(100);
         when(directory.getFieldValue(TiffTagConstants.TIFF_TAG_PHOTOMETRIC_INTERPRETATION))
             .thenReturn((short)2);
 
         // Act & Assert: triggers ImagingException from TiffImageParser         assertThrows(ImagingException.class,
             () -> new TiffImageParser().getBufferedImage(directory, ByteOrder.BIG_ENDIAN, mockedParams));
     }


     @Test
     public void testGetBufferedImage_samplesPerPixelMismatch() throws Exception {

         TiffImagingParameters params = new TiffImagingParameters();
 
         // Mock TiffDirectory
         TiffDirectory directory = mock(TiffDirectory.class);
         when(directory.findField(TiffTagConstants.TIFF_TAG_COMPRESSION)).thenReturn(null);
         when(directory.getSingleFieldValue(TiffTagConstants.TIFF_TAG_IMAGE_WIDTH)).thenReturn(100);
         when(directory.getSingleFieldValue(TiffTagConstants.TIFF_TAG_IMAGE_LENGTH)).thenReturn(100);
         when(directory.getFieldValue(TiffTagConstants.TIFF_TAG_PHOTOMETRIC_INTERPRETATION))
             .thenReturn((short)2);
 
         // Mock SAMPLES_PER_PIXEL => 1
         TiffField samplesPerPixelField = mock(TiffField.class);
         when(samplesPerPixelField.getIntValue()).thenReturn(1);
         when(directory.findField(TiffTagConstants.TIFF_TAG_SAMPLES_PER_PIXEL))
             .thenReturn(samplesPerPixelField);
 
         // bitsPerSample array => length=2 -> mismatch
         TiffField bitsPerSampleField = mock(TiffField.class);
         when(bitsPerSampleField.getIntArrayValue()).thenReturn(new int[] {8, 8});
         when(bitsPerSampleField.getIntValueOrArraySum()).thenReturn(16);
         when(directory.findField(TiffTagConstants.TIFF_TAG_BITS_PER_SAMPLE))
             .thenReturn(bitsPerSampleField);
 
         // Act & Assert => triggers ImagingException
         assertThrows(ImagingException.class,
             () -> new TiffImageParser().getBufferedImage(directory, ByteOrder.BIG_ENDIAN, params));
     }
 
 }