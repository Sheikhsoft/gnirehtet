/*
 * Copyright (C) 2017 Genymobile
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

package com.genymobile.gnirehtet;

import android.net.VpnService;
import android.util.Log;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class RelayTunnel implements Tunnel {

    private static final String TAG = RelayTunnel.class.getSimpleName();

    private static final int DEFAULT_PORT = 31416;

    private final SocketChannel channel;

    private RelayTunnel(SocketChannel channel) {
        this.channel = channel;
    }

    public static RelayTunnel open(VpnService vpnService) throws IOException {
        Log.d(TAG, "Opening a new relay tunnel...");
        SocketChannel channel = SocketChannel.open();
        vpnService.protect(channel.socket());
        channel.connect(new InetSocketAddress(Inet4Address.getLocalHost(), DEFAULT_PORT));
        return new RelayTunnel(channel);
    }

    @Override
    public void send(byte[] packet, int len) throws IOException {
        if (GnirehtetService.VERBOSE) {
            Log.d(TAG, "Sending..." + Binary.toString(packet, len));
        }
        ByteBuffer buffer = ByteBuffer.wrap(packet, 0, len);
        while (buffer.hasRemaining()) {
            channel.write(buffer);
        }
    }

    @Override
    public int receive(byte[] packet) throws IOException {
        int r = channel.read(ByteBuffer.wrap(packet));
        if (GnirehtetService.VERBOSE) {
            Log.d(TAG, "Receiving..." + Binary.toString(packet, r));
        }
        return r;
    }

    @Override
    public void close() {
        try {
            channel.close();
        } catch (IOException e) {
            // what could we do?
            throw new RuntimeException(e);
        }
    }
}
