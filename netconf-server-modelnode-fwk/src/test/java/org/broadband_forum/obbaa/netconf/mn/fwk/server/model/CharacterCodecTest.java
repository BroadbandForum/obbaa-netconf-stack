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

package org.broadband_forum.obbaa.netconf.mn.fwk.server.model;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import org.broadband_forum.obbaa.netconf.mn.fwk.server.model.util.CharacterCodec;

@RunWith(Parameterized.class)
public class CharacterCodecTest {
    private String m_text;
    private String m_encodedValue;

    @Test
    public void testCharacterCodecFunctionalityWithDifferentInputs() {
        final String encodedText = CharacterCodec.encode(m_text);
        assertEquals(m_encodedValue, encodedText);
        final String decodedText = CharacterCodec.decode(encodedText);
        assertEquals(m_text, decodedText);
    }

    public CharacterCodecTest(final String decodedValue, final String encodedValue) {
        this.m_text = decodedValue;
        this.m_encodedValue = encodedValue;
    }

    @Parameterized.Parameters
    public static Collection values() {
        return Arrays.asList(new Object[][]{
                {"=/\\", "\\=\\/\\\\"},
                {"plat=form", "plat\\=form"},
                {"/platform", "\\/platform"},
                {"platform\\", "platform\\\\"},
        });
    }
}
