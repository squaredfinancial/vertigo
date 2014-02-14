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
package net.kuujo.vertigo.feeder.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.impl.DefaultFutureResult;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Container;

import net.kuujo.vertigo.context.InstanceContext;
import net.kuujo.vertigo.feeder.Feeder;
import net.kuujo.vertigo.message.JsonMessage;
import net.kuujo.vertigo.message.MessageId;
import net.kuujo.vertigo.runtime.FailureException;
import net.kuujo.vertigo.runtime.TimeoutException;

/**
 * A distributed RPC feeder.
 *
 * @author Jordan Halterman
 */
public class DRPCFeeder extends BasicFeeder implements Feeder {
  protected InternalQueue queue;

  public DRPCFeeder(Vertx vertx, Container container, InstanceContext<Feeder> context) {
    super(vertx, container, context);
    queue = new InternalQueue(vertx);
  }

  private Handler<MessageId> ackHandler = new Handler<MessageId>() {
    @Override
    public void handle(MessageId messageId) {
      queue.ack(messageId);
    }
  };

  private Handler<MessageId> failHandler = new Handler<MessageId>() {
    @Override
    public void handle(MessageId messageId) {
      queue.fail(messageId);
    }
  };

  private Handler<MessageId> timeoutHandler = new Handler<MessageId>() {
    @Override
    public void handle(MessageId messageId) {
      queue.timeout(messageId);
    }
  };

  private Handler<JsonMessage> messageHandler = new Handler<JsonMessage>() {
    @Override
    public void handle(JsonMessage message) {
      input.ack(message);
      queue.result(message);
    }
  };

  @Override
  public Feeder start(Handler<AsyncResult<Feeder>> doneHandler) {
    output.ackHandler(ackHandler);
    output.failHandler(failHandler);
    output.timeoutHandler(timeoutHandler);
    input.messageHandler(messageHandler);
    return super.start(doneHandler);
  }

  /**
   * Sets the result timeout.
   *
   * @param timeout
   *   The result timeout.
   * @return
   *   The feeder instance.
   */
  public DRPCFeeder setResultTimeout(long timeout) {
    queue.replyTimeout = timeout;
    return this;
  }

  /**
   * Returns the result timeout.
   *
   * @return
   *   The result timeout.
   */
  public long getResultTimeout() {
    return queue.replyTimeout;
  }

  @Override
  public boolean feedQueueFull() {
    return queue.full() || super.feedQueueFull();
  }

  @Override
  public <T> MessageId emit(JsonObject args, Handler<AsyncResult<T>> resultHandler) {
    return doExecute(null, args, 0, resultHandler);
  }

  @Override
  public <T> MessageId emit(String stream, JsonObject args, Handler<AsyncResult<T>> resultHandler) {
    return doExecute(stream, args, 0, resultHandler);
  }

  /**
   * Performs an execution.
   */
  protected <T> MessageId doExecute(JsonObject args, Handler<AsyncResult<T>> resultHandler) {
    return doExecute(null, args, 0, resultHandler);
  }

  /**
   * Performs an execution.
   */
  protected <T> MessageId doExecute(String stream, JsonObject args, Handler<AsyncResult<T>> resultHandler) {
    return doExecute(stream, args, 0, resultHandler);
  }

  /**
   * Performs an execution.
   */
  private <T> MessageId doExecute(final String stream, final JsonObject args, final int attempts, final Handler<AsyncResult<T>> resultHandler) {
    final MessageId id = stream != null ? output.emitTo(stream, args) : output.emit(args);
    queue.enqueue(id, new Handler<AsyncResult<Collection<JsonMessage>>>() {
      @Override
      @SuppressWarnings("unchecked")
      public void handle(AsyncResult<Collection<JsonMessage>> result) {
        if (autoRetry && (retryAttempts == AUTO_RETRY_ATTEMPTS_UNLIMITED || attempts < retryAttempts)
            && result.failed() && result.cause() instanceof TimeoutException) {
          doExecute(stream, args, attempts+1, resultHandler);
        }
        else {
          resultHandler.handle((AsyncResult<T>) result);
        }
      }
    });
    fed = true; checkPause();
    return id;
  }

  /**
   * An internal execute queue.
   */
  private static class InternalQueue {
    // Set up failure and timeout exceptions without stack traces. This prevents
    // us from having to create exceptions repeatedly which would otherwise result
    // in the stack trace being filled in frequently.
    private static final FailureException FAILURE_EXCEPTION = new FailureException("Processing failed.");
    static { FAILURE_EXCEPTION.setStackTrace(new StackTraceElement[0]); }

    private static final TimeoutException TIMEOUT_EXCEPTION = new TimeoutException("Processing timed out.");
    static { TIMEOUT_EXCEPTION.setStackTrace(new StackTraceElement[0]); }

    private final Vertx vertx;
    private final Map<String, FutureResult> futures = new HashMap<>();
    private long replyTimeout = 30000;
    private long maxSize = 1000;

    private InternalQueue(Vertx vertx) {
      this.vertx = vertx;
    }

    /**
     * Holds execute futures.
     */
    private static class FutureResult {
      private final long timer;
      private final Handler<AsyncResult<Collection<JsonMessage>>> handler;
      private List<JsonMessage> results = new ArrayList<>();

      public FutureResult(long timer, Handler<AsyncResult<Collection<JsonMessage>>> handler) {
        this.timer = timer;
        this.handler = handler;
      }
    }

    /**
     * Returns the execute queue size.
     */
    private final int size() {
      return futures.size();
    }

    /**
     * Indicates whether the execute queue is full.
     */
    private final boolean full() {
      return size() > maxSize;
    }

    /**
     * Enqueues a new item in the execute queue. When the item is acked or failed
     * by ID, or when a result is received, the appropriate handlers will be called.
     */
    private void enqueue(final MessageId id, Handler<AsyncResult<Collection<JsonMessage>>> resultHandler) {
      long timerId = vertx.setTimer(replyTimeout, new Handler<Long>() {
        @Override
        public void handle(Long timerId) {
          FutureResult future = futures.get(id.correlationId());
          if (future != null) {
            new DefaultFutureResult<Collection<JsonMessage>>(TIMEOUT_EXCEPTION).setHandler(futures.remove(id.correlationId()).handler);
          }
        }
      });
      futures.put(id.correlationId(), new FutureResult(timerId, resultHandler));
    }

    /**
     * Acks an item in the queue.
     */
    private void ack(MessageId id) {
      FutureResult future = futures.remove(id.correlationId());
      if (future != null) {
        vertx.cancelTimer(future.timer);
        new DefaultFutureResult<Collection<JsonMessage>>(future.results).setHandler(future.handler);
      }
    }

    /**
     * Fails an item in the queue.
     */
    private void fail(MessageId id) {
      FutureResult future = futures.remove(id.correlationId());
      if (future != null) {
        vertx.cancelTimer(future.timer);
        new DefaultFutureResult<Collection<JsonMessage>>(FAILURE_EXCEPTION).setHandler(future.handler);
      }
    }

    /**
     * Times out an item in the queue.
     */
    private void timeout(MessageId id) {
      FutureResult future = futures.remove(id.correlationId());
      if (future != null) {
        vertx.cancelTimer(future.timer);
        new DefaultFutureResult<Collection<JsonMessage>>(TIMEOUT_EXCEPTION).setHandler(future.handler);
      }
    }

    /**
     * Sets the result of an item in the queue.
     */
    private void result(JsonMessage message) {
      FutureResult future = futures.get(message.messageId().root());
      if (future != null) {
        future.results.add(message);
      }
    }
  }

}
