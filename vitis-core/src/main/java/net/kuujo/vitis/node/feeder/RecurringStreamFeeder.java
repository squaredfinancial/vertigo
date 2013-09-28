/*
* Copyright 2013 the original author or authors.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package net.kuujo.vitis.node.feeder;

import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;

/**
 * A recurring stream feeder implementation.
 *
 * @author Jordan Halterman
 */
public class RecurringStreamFeeder implements StreamFeeder<RecurringStreamFeeder>, RecurringFeeder<RecurringStreamFeeder> {

  @Override
  public RecurringStreamFeeder setFeedQueueMaxSize(long maxSize) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public long getFeedQueueMaxSize() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public boolean feedQueueFull() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public RecurringStreamFeeder feed(JsonObject data) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public RecurringStreamFeeder feed(JsonObject data, String tag) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public RecurringStreamFeeder autoRetry(boolean retry) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public boolean autoRetry() {
    // TODO Auto-generated method stub
    return false;
  }

  @Override
  public RecurringStreamFeeder retryAttempts(int attempts) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public int retryAttempts() {
    // TODO Auto-generated method stub
    return 0;
  }

  @Override
  public RecurringStreamFeeder drainHandler(Handler<Void> handler) {
    // TODO Auto-generated method stub
    return null;
  }

}
