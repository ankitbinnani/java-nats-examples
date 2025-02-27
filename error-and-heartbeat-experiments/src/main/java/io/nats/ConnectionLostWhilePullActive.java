// Copyright 2023 The NATS Authors
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at:
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package io.nats;

import io.nats.client.*;

public class ConnectionLostWhilePullActive {
    public static void main(String[] args) {
        Options options = new Options.Builder()
            .server(Options.DEFAULT_URL)
            .errorListener(new ExampleErrorListener())
            .build();

        try (Connection nc = Nats.connect(options)) {
            JetStream js = nc.jetStream();
            JetStreamManagement jsm = nc.jetStreamManagement();

            // Create the stream.
            Utils.createTestStream(jsm);

            // Setup pull subscriptions. Could be durable, but not required for example.
            // Ephemerals that don't specify inactive threshold default to 5 seconds.
            JetStreamSubscription syncSub = js.subscribe(Utils.SUBJECT,
                PullSubscribeOptions.builder()
                    .name(Utils.SYNC_CONSUMER).build());

            Dispatcher d = nc.createDispatcher();
            JetStreamSubscription callbackSub = js.subscribe(Utils.SUBJECT, d, Message::ack,
                PullSubscribeOptions.builder()
                    .name(Utils.CALLBACK_CONSUMER).build());

            // Pull with long expiration.
            // No messages have been published to the subject,
            // so it will just wait to expire.
            PullRequestOptions pro = PullRequestOptions.builder(1)
                .expiresIn(30000)
                .idleHeartbeat(1000)
                .build();
            syncSub.pull(pro);
            callbackSub.pull(pro);

            System.out.println("\n" +
                "------------------------------------------------------------------------------------------------------\n" +
                "Experiment 1. Kill server with the consumer leader if it's not the same server you are connected to...\n" +
                "Look at the console for something like\n" +
                "    \"JetStream cluster new consumer leader for '$G > TheStream > SyncConsumer'\"\n" +
                "or use the NATS Cli 'nats c info' command\n" +
                "------------------------------------------------------------------------------------------------------\n" +
                "Experiment 2. Kill server with you are connected to if it's not the same as the consumer leader...\n" +
                "------------------------------------------------------------------------------------------------------\n" +
                "Experiment 3. Kill server with you are connected if it is the same as the consumer leader...\n" +
                "------------------------------------------------------------------------------------------------------\n"
            );
            Thread.sleep(10000);

            // Both Sync and Callback subscriptions get messages sent to the error listener.
            // Sync subscriptions throw exceptions on errors.
            try {
                syncSub.nextMessage(1000);
            }
            catch (JetStreamStatusException e) {
                System.err.println("Sync Exception: " + e.getStatus().toString());
            }

            // Make sure the error listener has time to get the errors
            // and alarms before closing the connection.
            Thread.sleep(1000);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
