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
package net.kuujo.vertigo.java;

import java.util.Collection;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;

import net.kuujo.vertigo.annotations.ExecutorOptions;
import net.kuujo.vertigo.component.ComponentFactory;
import net.kuujo.vertigo.component.impl.DefaultComponentFactory;
import net.kuujo.vertigo.context.InstanceContext;
import net.kuujo.vertigo.message.JsonMessage;
import net.kuujo.vertigo.message.MessageId;
import net.kuujo.vertigo.rpc.Executor;
import net.kuujo.vertigo.runtime.FailureException;
import net.kuujo.vertigo.runtime.TimeoutException;

/**
 * A rich executor verticle implementation.
 *
 * @author Jordan Halterman
 */
public abstract class RichExecutorVerticle extends ComponentVerticle<Executor> {
  protected Executor executor;

  @Override
  protected Executor createComponent(InstanceContext<Executor> context) {
    ComponentFactory componentFactory = new DefaultComponentFactory(vertx, container);
    return componentFactory.createExecutor(context);
  }

  @Override
  protected void start(Executor executor) {
    this.executor = setupExecutor(executor);
    executor.executeHandler(new Handler<Executor>() {
      @Override
      public void handle(Executor executor) {
        nextMessage();
      }
    });
  }

  /**
   * Sets up the executor according to executor options.
   */
  private Executor setupExecutor(Executor executor) {
    ExecutorOptions options = getClass().getAnnotation(ExecutorOptions.class);
    if (options != null) {
      executor.setResultTimeout(options.resultTimeout());
      executor.setExecuteQueueMaxSize(options.executeQueueMaxSize());
      executor.setAutoRetry(options.autoRetry());
      executor.setAutoRetryAttempts(options.autoRetryAttempts());
      executor.setExecuteInterval(options.executeInterval());
    }
    return executor;
  }

  private Handler<AsyncResult<JsonMessage>> wrapHandler(final Handler<AsyncResult<JsonMessage>> resultHandler) {
    return new Handler<AsyncResult<JsonMessage>>() {
      @Override
      public void handle(AsyncResult<JsonMessage> result) {
        if (result.failed()) {
          if (result.cause() instanceof FailureException) {
            handleFailure((FailureException) result.cause());
          }
          else if (result.cause() instanceof TimeoutException) {
            handleTimeout((TimeoutException) result.cause());
          }
        }
        else {
          handleResult(result.result());
        }
        if (resultHandler != null) {
          resultHandler.handle(result);
        }
      }
    };
  }

  private Handler<AsyncResult<Collection<JsonMessage>>> wrapHandlerCollecting(final Handler<AsyncResult<Collection<JsonMessage>>> resultHandler) {
      return new Handler<AsyncResult<Collection<JsonMessage>>>() {
        @Override
        public void handle(AsyncResult<Collection<JsonMessage>> result) {
          if (result.failed()) {
            if (result.cause() instanceof FailureException) {
              handleFailure((FailureException) result.cause());
            }
            else if (result.cause() instanceof TimeoutException) {
              handleTimeout((TimeoutException) result.cause());
            }
          }
          else {
            handleResultsCollected(result.result());
          }
          if (resultHandler != null) {
            resultHandler.handle(result);
          }
        }
      };
    }

  /**
   * Called when the executor is requesting the next message.
   *
   * Override this method to perform polling-based executions. The executor will automatically
   * call this method any time the execute queue is prepared to accept new messages.
   */
  protected void nextMessage() {
  }

  /**
   * Indicates whether the execute queue is full.
   *
   * @return
   *   Indicates whether the queue is full.
   */
  public boolean executeQueueFull() {
    return executor.executeQueueFull();
  }

  /**
   * Executes a message.
   *
   * @param args
   *   The output message body.
   * @return
   *   The output message identifier.
   */
  public MessageId execute(JsonObject args) {
    return executor.execute(args, wrapHandler(null));
  }

  /**
   * Executes a message.
   *
   * @param stream
   *   The stream to which to emit the message.
   * @param args
   *   The output message body.
   * @return
   *   The output message identifier.
   */
  public MessageId execute(String stream, JsonObject args) {
    return executor.execute(stream, args, wrapHandler(null));
  }

  /**
   * Executes a message.
   *
   * @param args
   *   The output message body.
   * @param resultHandler
   *   An asynchronous handler to be called with the execution result.
   * @return
   *   The output message identifier.
   */
  public MessageId execute(JsonObject args, Handler<AsyncResult<JsonMessage>> resultHandler) {
    return executor.execute(args, wrapHandler(resultHandler));
  }

  /**
   * Executes a message.
   *
   * @param stream
   *   The stream to which to emit the message.
   * @param args
   *   The output message body.
   * @param resultHandler
   *   An asynchronous handler to be called with the execution result.
   * @return
   *   The output message identifier.
   */
  public MessageId execute(String stream, JsonObject args, Handler<AsyncResult<JsonMessage>> resultHandler) {
    return executor.execute(stream, args, wrapHandler(resultHandler));
  }

  /**
   * Executes a message, aggregating multiple results.
   *
   * @param args
   *   The output message body.
   * @return
   *   The output message identifier.
   */
  public MessageId executeCollecting(JsonObject args) {
    return executor.executeCollecting(args, wrapHandlerCollecting(null));
  }

  /**
   * Executes a message, aggregating multiple results.
   *
   * @param stream
   *   The stream to which to emit the message.
   * @param args
   *   The output message body.
   * @return
   *   The output message identifier.
   */
  public MessageId executeCollecting(String stream, JsonObject args) {
    return executor.executeCollecting(stream, args, wrapHandlerCollecting(null));
  }

  /**
   * Executes a message, aggregating multiple results.
   *
   * @param args
   *   The output message body.
   * @param resultHandler
   *   An asynchronous handler to be called with the execution result.
   * @return
   *   The output message identifier.
   */
  public MessageId executeCollecting(JsonObject args, Handler<AsyncResult<Collection<JsonMessage>>> resultHandler) {
    return executor.executeCollecting(args, wrapHandlerCollecting(resultHandler));
  }

  /**
   * Executes a message, aggregating multiple results.
   *
   * @param stream
   *   The stream to which to emit the message.
   * @param args
   *   The output message body.
   * @param resultHandler
   *   An asynchronous handler to be called with the execution result.
   * @return
   *   The output message identifier.
   */
  public MessageId executeCollecting(String stream, JsonObject args, Handler<AsyncResult<Collection<JsonMessage>>> resultHandler) {
    return executor.executeCollecting(stream, args, wrapHandlerCollecting(resultHandler));
  }

  /**
   * Called when an execute()ed message is successfully processed and a result is received.
   *
   * Override this method to provide custom handling for execution results.
   *
   * This method is not called on results of messages sent by executeCollecting()
   *
   * @param result
   *   The result message.
   */
  protected void handleResult(JsonMessage result) {
  }

  /**
   * Called when an executeCollecting()ed message is successfully processed and collected results are received.
   *
   * Override this method to provide custom handling for execution results.
   *
   * @param result
   *   The result message.
   */
  protected void handleResultsCollected(Collection<JsonMessage> results) {
  }

  /**
   * Called when a message is explicitly failed.
   *
   * Override this method to provide custom handling for message failures.
   */
  protected void handleFailure(FailureException cause) {
  }

  /**
   * Called when a message times out.
   *
   * Override this method to provide custom handling for message timeouts.
   */
  protected void handleTimeout(TimeoutException cause) {
  }

}
