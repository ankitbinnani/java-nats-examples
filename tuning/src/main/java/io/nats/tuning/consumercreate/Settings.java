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

package io.nats.tuning.consumercreate;

import io.nats.client.Options;
import io.nats.client.api.StorageType;
import io.nats.tuning.support.OptionsBuilderFactory;
import io.nats.tuning.support.SubjectGenerator;
import io.nats.tuning.support.UniqueSubjectGenerator;

public class Settings {

    public OptionsBuilderFactory optionsBuilder = () ->
        Options.builder().server("localhost:4222,localhost:5222,localhost:6222");

    public long verifyConnectMs = 10000; // < 0 means do not verify

    public String streamName = "stream";
    public StorageType storageType = StorageType.File;
    public int replicas = 3;

    public SubjectGenerator subjectGenerator = new UniqueSubjectGenerator();

    public long timeoutMs = 20_000; // 20 seconds

    public int appInstances = 10;
    public int threadsPerApp = 10;
    public int consumersPerApp = 100;
    public long beforeCreateDelayMs = 20;
    public long beforeSubDelayMs = 20;

    public long inactiveThresholdMs = timeoutMs * 2;
    public AppStrategy appStrategy = AppStrategy.Client_Api_Subscribe;
    public SubStrategy subStrategy = SubStrategy.Push_Provide_Stream;

    public int publishInstances = 10;
    public int payloadSize = 100;
    public int pauseAfterStartPublishingMs = 2000;

    public int reportFrequency = 1;
    public float autoReportFactor = 0.2f;

    public boolean reportNanos = false;

    public long time(long t) {
        return reportNanos ? t : t / 1_000_000;
    }

    public String timeLabel() {
        return reportNanos ? "ns" : "ms";
    }

    public void autoReportFrequency() {
        reportFrequency = Math.max(1, (int) (consumersPerApp / threadsPerApp * autoReportFactor));
    }

    public void validate() {
        if (!isValid()) {
            System.err.println(subStrategy.name().replace("_", " ") + " not allowed for " + appStrategy.name().replace("_", " "));
            System.exit(-1);
        }
    }
    public boolean isValid() {
        if (appStrategy == AppStrategy.Client_Api_Subscribe) {
            if (subStrategy == SubStrategy.Push_Bind || subStrategy == SubStrategy.Pull_Bind) {
                return false;
            }
        }
        return true;
    }
}
