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

import net.kuujo.vitis.context.WorkerContext;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Container;

/**
 * A default stream feeder implementation.
 *
 * @author Jordan Halterman
 */
public class DefaultStreamFeeder extends AbstractFeeder<StreamFeeder> implements StreamFeeder {

  private static final long START_DELAY = 1000;

  private Handler<StreamFeeder> connectHandler;

  private Handler<Void> fullHandler;

  private Handler<Void> drainHandler;

  private boolean started;

  private boolean paused;

  public DefaultStreamFeeder(Vertx vertx, Container container, WorkerContext context) {
    super(vertx, container, context);
  }

  @Override
  public boolean feedQueueFull() {
    return !started || paused;
  }

  @Override
  public void start() {
    vertx.setTimer(START_DELAY, new Handler<Long>() {
      @Override
      public void handle(Long timerID) {
        started = true;
        if (connectHandler != null) {
          connectHandler.handle(DefaultStreamFeeder.this);
        }
      }
    });
  }

  @Override
  public StreamFeeder connectHandler(Handler<StreamFeeder> handler) {
    connectHandler = handler;
    return this;
  }

  @Override
  public StreamFeeder fullHandler(Handler<Void> handler) {
    fullHandler = handler;
    return this;
  }

  @Override
  public StreamFeeder drainHandler(Handler<Void> handler) {
    drainHandler = handler;
    return this;
  }

  @Override
  public StreamFeeder feed(JsonObject data) {
    doFeed(data, null, 0, new DefaultFutureResult<Void>().setHandler(createAckHandler(null)));
    checkPause();
    return this;
  }

  @Override
  public StreamFeeder feed(JsonObject data, String tag) {
    doFeed(data, tag, 0, new DefaultFutureResult<Void>().setHandler(createAckHandler(null)));
    checkPause();
    return this;
  }

  @Override
  public StreamFeeder feed(JsonObject data, Handler<AsyncResult<Void>> ackHandler) {
    doFeed(data, null, 0, new DefaultFutureResult<Void>().setHandler(createAckHandler(ackHandler)));
    checkPause();
    return this;
  }

  @Override
  public StreamFeeder feed(JsonObject data, String tag, Handler<AsyncResult<Void>> ackHandler) {
    doFeed(data, tag, 0, new DefaultFutureResult<Void>().setHandler(createAckHandler(ackHandler)));
    checkPause();
    return this;
  }

  /**
   * Creates a message ack handler.
   */
  private Handler<AsyncResult<Void>> createAckHandler(final Handler<AsyncResult<Void>> ackHandler) {
    return new Handler<AsyncResult<Void>>() {
      @Override
      public void handle(AsyncResult<Void> result) {
        checkPause();
        if (ackHandler != null) {
          if (result.failed()) {
            new DefaultFutureResult<Void>().setHandler(ackHandler).setFailure(result.cause());
          }
          else {
            new DefaultFutureResult<Void>().setHandler(ackHandler).setResult(result.result());
          }
        }
      }
    };
  }

  /**
   * Checks the current stream pause status.
   */
  private void checkPause() {
    if (paused) {
      if (queue.size() < queue.maxQueueSize() * .75) {
        paused = false;
        if (drainHandler != null) {
          drainHandler.handle(null);
        }
      }
    }
    else {
      if (queue.size() >= queue.maxQueueSize()) {
        paused = true;
        if (fullHandler != null) {
          fullHandler.handle(null);
        }
      }
    }
  }

}