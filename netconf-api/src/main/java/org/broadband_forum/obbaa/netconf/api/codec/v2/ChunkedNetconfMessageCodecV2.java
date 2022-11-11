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

package org.broadband_forum.obbaa.netconf.api.codec.v2;

import static org.broadband_forum.obbaa.netconf.api.util.ByteBufUtils.releaseByteBuf;
import static org.broadband_forum.obbaa.netconf.api.util.ByteBufUtils.unpooledHeapByteBuf;
import static org.broadband_forum.obbaa.netconf.api.util.NetconfResources.CHUNK_SIZE;
import static org.broadband_forum.obbaa.netconf.api.util.NetconfResources.MAXIMUM_SIZE_OF_CHUNKED_MESSAGES;

import java.nio.charset.StandardCharsets;

import org.broadband_forum.obbaa.netconf.api.LogAppNames;
import org.broadband_forum.obbaa.netconf.api.MessageToolargeException;
import org.broadband_forum.obbaa.netconf.api.messages.DocumentToPojoTransformer;
import org.broadband_forum.obbaa.netconf.api.util.DocumentUtils;
import org.broadband_forum.obbaa.netconf.api.util.NetconfMessageBuilderException;
import org.broadband_forum.obbaa.netconf.api.utils.SystemPropertyUtils;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLogger;
import org.broadband_forum.obbaa.netconf.stack.logging.AdvancedLoggerUtil;
import org.w3c.dom.Document;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;

public class ChunkedNetconfMessageCodecV2 implements NetconfMessageCodecV2 {
    private static final AdvancedLogger LOGGER = AdvancedLoggerUtil.getGlobalDebugLogger(ChunkedNetconfMessageCodecV2.class, LogAppNames.NETCONF_LIB);
    private int m_maxChunkSize;
    private ChunkMsgState m_state;
    private CompositeByteBuf m_chunk;
    private int m_chunkSize;
    private StringBuilder m_chunkString = new StringBuilder();
    private int m_chunkSizeForEncoding;

    public ChunkedNetconfMessageCodecV2() {
        m_maxChunkSize = Integer.parseInt(SystemPropertyUtils.getInstance().getFromEnvOrSysProperty(MAXIMUM_SIZE_OF_CHUNKED_MESSAGES, "134217728"));
        m_chunkSizeForEncoding = Integer.parseInt(SystemPropertyUtils.getInstance().getFromEnvOrSysProperty(CHUNK_SIZE, "65536"));
        m_state = ChunkMsgState.HEADER_ONE;
    }

    ChunkedNetconfMessageCodecV2(int maxSizeOfChunkMsg, int chunkSize) {
        m_maxChunkSize = maxSizeOfChunkMsg;
        m_chunkSizeForEncoding = chunkSize;
        m_state = ChunkMsgState.HEADER_ONE;
    }

    /*
    Example of a chunked netconf message with delimiter is as below:
    \n#14\n<rpc-example/>\n##\n
     */
    private enum ChunkMsgState {
        HEADER_ONE, // \n
        HEADER_TWO, // #
        CHUNK_LENGTH_FIRST, // [1-9]
        CHUNK_LENGTH_REST, // [0-9]*\n
        CHUNK_DATA,
        FOOTER_ONE, // \n
        FOOTER_TWO, // #
        FOOTER_THREE, // #
        FOOTER_FOUR, // \n
    }

    @Override
    public byte[] encode(Document msg) throws NetconfMessageBuilderException {
        return DocumentToPojoTransformer.chunkMessage(m_chunkSizeForEncoding, DocumentUtils.documentToString(msg)).getBytes();
    }

    /*
    Decoding a chunked netconf message occurs based on the state of the characters present in the message.
    If we consider example \n#14\n<rpc-example/>\n##\n ,the decoding starts with state HEADER_ONE the first character of
    the byte buf is read to see, if it is a linefeed, if not exception is thrown else, the state is moved to HEADER_TWO.
    Next byte is read to see if it is # ,if so, the state is moved to CHUNK_LENGTH_FIRST and CHUNK_LENGTH_REST, to get the
    chunk length to be read. In the example it is 14 . After reading the chunk size to be read, the state is moved to CHUNK_DATA.
    Here, the chunk of size fetched from the previous state will be read from the byte buf i.e for the example 14 bytes
    of data will be read from the byte buf. Next the state is moved to FOOTER_ONE to see if he next byte is linefeed followed
    by state FOOTER_TWO to see if subsequent byte is #. If so, the state is moved to FOOTER_THREE. Here either next chunk
    can be present or the End of Chunk can be present. If next chunk is present , we go back to reading the chunk length, else
    the state is moved to FOOTER_FOUR where we verify that the byte is linefeed. Also we return the RPC document
    read till End of Chunk. In case of example <rpc-example/> is returned.
     */
    @Override
    public synchronized DocumentInfo decode(ByteBuf in) throws NetconfMessageBuilderException, MessageToolargeException {
        while (in.isReadable()) {
            switch (m_state) {
                case HEADER_ONE: {
                    final byte b = in.readByte();
                    if (ignoreChar(b, ' ')) {
                        break;
                    }
                    checkExpectedCharacter(b, '\n');
                    m_state = ChunkMsgState.HEADER_TWO;
                    break;
                }
                case HEADER_TWO: {
                    final byte b = in.readByte();
                    if (ignoreChar(b, ' ')) {
                        m_state = ChunkMsgState.HEADER_ONE;
                        break;
                    }
                    if (ignoreChar(b, '\n')) {
                        break;
                    }
                    checkExpectedCharacter(b, '#');
                    m_state = ChunkMsgState.CHUNK_LENGTH_FIRST;
                    break;
                }
                case CHUNK_LENGTH_FIRST: {
                    final byte b = in.readByte();
                    m_chunkSize = fetchChunkSizeFromChunkFirstLength(b);
                    m_state = ChunkMsgState.CHUNK_LENGTH_REST;
                    break;
                }
                case CHUNK_LENGTH_REST: {
                    final byte b = in.readByte();
                    if (b == '\n') {
                        m_state = ChunkMsgState.CHUNK_DATA;
                        break;
                    }
                    checkHeaderLengthInRange(b, '0', '9');
                    incrementChunkSize(b);
                    break;
                }
                case CHUNK_DATA: {
                    if (in.readableBytes() < m_chunkSize) {
                        if(LOGGER.isDebugEnabled()) {
                            LOGGER.debug("going to discard the bytes {} read till now to fetch complete chunk of size {}", in.readableBytes(), m_chunkSize);
                        }
                        in.discardReadBytes();
                        return null;
                    }
                    m_chunkString.append(in.readCharSequence(m_chunkSize, StandardCharsets.UTF_8).toString());
                    checkChunkMsgSizeExceedsMax();
                    m_state = ChunkMsgState.FOOTER_ONE;
                    break;
                }
                case FOOTER_ONE: {
                    final byte b = in.readByte();
                    checkExpectedCharacter(b, '\n');
                    m_state = ChunkMsgState.FOOTER_TWO;
                    break;
                }
                case FOOTER_TWO: {
                    final byte b = in.readByte();
                    if (ignoreChar(b, ' ')) {
                        break;
                    }
                    if (ignoreChar(b, '\n')) {
                        break;
                    }
                    checkExpectedCharacter(b, '#');
                    m_state = ChunkMsgState.FOOTER_THREE;
                    m_chunkSize = 0;
                    break;
                }
                case FOOTER_THREE: {
                    final byte b = in.readByte();
                    //to check if it is #(ChunkEOM) or chunking is continued
                    fetchChunkEomOrNextChunk(b);
                    break;
                }
                case FOOTER_FOUR: {
                    final byte b = in.readByte();
                    checkExpectedCharacter(b, '\n');
                    m_state = ChunkMsgState.HEADER_ONE;
                    String rpcString = m_chunkString.toString();
                    m_chunkString = new StringBuilder();
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("The content of the rpc after decoding is {}", LOGGER.sensitiveData(rpcString));
                    }
                    return new DocumentInfo(DocumentUtils.stringToDocument(rpcString), rpcString);
                }
                default:
                    LOGGER.warn("Invalid Chunk State");
                    throw new IllegalStateException(String.format("Got invalid state %s", m_state));
            }
        }
        return null;
    }

    private boolean ignoreChar(byte b, char ignorableChar) {
        return b == ignorableChar;
    }

    private void fetchChunkEomOrNextChunk(byte byteBeingRead) {
        if (byteBeingRead == '#') {
            m_state = ChunkMsgState.FOOTER_FOUR;
        } else if (isChunkFirstLengthInRange(byteBeingRead)) {
            m_chunkSize = fetchChunkSizeFromChunkFirstLength(byteBeingRead);
            m_state = ChunkMsgState.CHUNK_LENGTH_REST;
        } else {
            LOGGER.error("Expected either byte({}) OR from byte({}) to byte({}), but got byte({})", (byte) '#', (byte) '1', (byte) '9', byteBeingRead);
            throw new IllegalArgumentException(String.format("Got invalid value: %s, byte: %s", (char) byteBeingRead, byteBeingRead));
        }
    }

    private void incrementChunkSize(byte b) {
        m_chunkSize *= 10;
        m_chunkSize += b - '0';
    }

    private void appendToChunk(final ByteBuf nextChunk) {
        m_chunk.addComponent(true, m_chunk.numComponents(), nextChunk);
    }

    private int fetchChunkSizeFromChunkFirstLength(final byte byteBeingRead) {
        checkHeaderLengthInRange(byteBeingRead, '1', '9');
        return byteBeingRead - '0';
    }

    private void checkExpectedCharacter(final byte byteBeingRead, char expectedChar) {
        if (byteBeingRead != expectedChar) {
            //The error messages are written in accordance with ASCII table
            LOGGER.error("Expected {}, but got {}", expectedChar, (char) byteBeingRead);
            throw new IllegalArgumentException(String.format("Got invalid character %s (byte)%s, while expecting %s", (char) byteBeingRead, byteBeingRead, expectedChar));
        }
    }

    private void checkChunkMsgSizeExceedsMax() throws MessageToolargeException {
        int chunkMsgSize = m_chunkString.length();
        if (chunkMsgSize > m_maxChunkSize) {
            LOGGER.error("Incoming message too long. Not decoding the message further to avoid " +
                    "buffer overrun, message size: {} max-size: {}", chunkMsgSize, m_maxChunkSize);
            throw new MessageToolargeException(String.format("Incoming message too long. Not decoding the message further to avoid " +
                    "buffer overrun, message size: %s max-size: %s", chunkMsgSize, m_maxChunkSize));
        }
    }

    private void initializeChunk() {
        releaseByteBuf(m_chunk);
        m_chunk = unpooledHeapByteBuf();
    }

    private void checkHeaderLengthInRange(final byte byteBeingRead, char lowerRange, char upperRange) {
        if (!(byteBeingRead >= lowerRange && byteBeingRead <= upperRange)) {
            //The error messages are written in accordance with ASCII table
            LOGGER.error("Range expected for header length is {} to {}, obtained {}", lowerRange, upperRange, (char) byteBeingRead);
            throw new IllegalArgumentException(String.format("Got invalid value for chunk header: %s, byte: %s", (char) byteBeingRead, byteBeingRead));
        }
    }

    private static boolean isChunkFirstLengthInRange(final byte byteBeingRead) {
        return byteBeingRead >= '1' && byteBeingRead <= '9';
    }
}
