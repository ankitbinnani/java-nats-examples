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

public enum SubStrategy {
    Push_Without_Stream(false),
    Push_Provide_Stream(false),
    Push_Bind(false),
    Pull_Without_Stream(true),
    Pull_Provide_Stream(true),
    Pull_Bind(true);

    public final boolean pull;

    SubStrategy(boolean pull) {
        this.pull = pull;
    }
}
