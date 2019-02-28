/*
 * Copyright 2018 Broadband Forum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.broadband_forum.obbaa.netconf.server.model.notification.utils;

import java.io.InputStream;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.broadband_forum.obbaa.netconf.api.server.notification.Stream;
import org.broadband_forum.obbaa.netconf.server.model.notification.Streams;

public class StreamUtil {


	public static final String DEFAULT_STREAMS_XML_FILE = "/streams.xml";
	
	/**
	 * @param streamsXmlFile
	 */
	public static List<Stream> loadStreamList(String streamsXmlFile) {
		try {
			InputStream inputStream = StreamUtil.class.getResourceAsStream(streamsXmlFile);
			JAXBContext jaxbContext = JAXBContext.newInstance(Streams.class);
			Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
			Streams streams = (Streams)unmarshaller.unmarshal(inputStream);
			return streams.getStreamList();
		} catch (JAXBException e) {
			throw new RuntimeException("Cannot unmarshal xml", e);
		}
	}

	

}
