/*
 * Copyright 2014 the original author or authors.
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
package net.kuujo.vertigo.cluster;

import net.kuujo.vertigo.cluster.data.AsyncIdGenerator;
import net.kuujo.vertigo.cluster.data.AsyncList;
import net.kuujo.vertigo.cluster.data.AsyncLock;
import net.kuujo.vertigo.cluster.data.AsyncQueue;
import net.kuujo.vertigo.cluster.data.AsyncSet;
import net.kuujo.vertigo.cluster.data.WatchableAsyncMap;

import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.json.JsonObject;

/**
 * Vertigo cluster coordinator.
 *
 * @author Jordan Halterman
 */
public interface VertigoCluster {

  /**
   * Checks whether a module or verticle is deployed in the cluster.
   *
   * @param deploymentID The deployment ID to check.
   * @param resultHandler An asynchronous handler to be called with a result.
   * @return The cluster client.
   */
  VertigoCluster isDeployed(String deploymentID, Handler<AsyncResult<Boolean>> resultHandler);

  /**
   * Deploys a module to the cluster.
   *
   * @param deploymentID The unique module deployment ID.
   * @param moduleName The module name.
   * @param config The module configuration.
   * @param instances The number of instances to deploy.
   * @param doneHandler An asynchronous handler to be called once deployment is complete.
   * @return The cluster client.
   */
  VertigoCluster deployModule(String deploymentID, String moduleName, JsonObject config, int instances, Handler<AsyncResult<String>> doneHandler);

  /**
   * Deploys a module to a specific HA group in the cluster.
   *
   * @param deploymentID The unique module deployment ID.
   * @param groupID The group to which to deploy the module.
   * @param moduleName The module name.
   * @param config The module configuration.
   * @param instances The number of instances to deploy.
   * @param doneHandler An asynchronous handler to be called once deployment is complete.
   * @return The cluster client.
   */
  VertigoCluster deployModuleTo(String deploymentID, String groupID, String moduleName, JsonObject config, int instances, Handler<AsyncResult<String>> doneHandler);

  /**
   * Deploys a verticle to the cluster.
   *
   * @param deploymentID The unique verticle deployment ID.
   * @param main The verticle main.
   * @param config The module configuration.
   * @param instances The number of instances to deploy.
   * @param doneHandler An asynchronous handler to be called once deployment is complete.
   * @return The cluster client.
   */
  VertigoCluster deployVerticle(String deploymentID, String main, JsonObject config, int instances, Handler<AsyncResult<String>> doneHandler);

  /**
   * Deploys a verticle to the cluster.
   *
   * @param deploymentID The unique verticle deployment ID.
   * @param groupID The group to which to deploy the verticle.
   * @param main The verticle main.
   * @param config The module configuration.
   * @param instances The number of instances to deploy.
   * @param doneHandler An asynchronous handler to be called once deployment is complete.
   * @return The cluster client.
   */
  VertigoCluster deployVerticleTo(String deploymentID, String groupID, String main, JsonObject config, int instances, Handler<AsyncResult<String>> doneHandler);

  /**
   * Deploys a worker verticle to the cluster.
   *
   * @param deploymentID The unique verticle deployment ID.
   * @param main The verticle main.
   * @param config The module configuration.
   * @param instances The number of instances to deploy.
   * @param multiThreaded Indicates whether the verticle is multi-threaded.
   * @param doneHandler An asynchronous handler to be called once deployment is complete.
   * @return The cluster client.
   */
  VertigoCluster deployWorkerVerticle(String deploymentID, String main, JsonObject config, int instances, boolean multiThreaded, Handler<AsyncResult<String>> doneHandler);

  /**
   * Deploys a worker verticle to the cluster.
   *
   * @param deploymentID The unique verticle deployment ID.
   * @param groupID The group to which to deploy the verticle.
   * @param main The verticle main.
   * @param config The module configuration.
   * @param instances The number of instances to deploy.
   * @param multiThreaded Indicates whether the verticle is multi-threaded.
   * @param doneHandler An asynchronous handler to be called once deployment is complete.
   * @return The cluster client.
   */
  VertigoCluster deployWorkerVerticleTo(String deploymentID, String groupID, String main, JsonObject config, int instances, boolean multiThreaded, Handler<AsyncResult<String>> doneHandler);

  /**
   * Undeploys a module from the cluster.
   *
   * @param deploymentID The unique module deployment ID.
   * @param doneHandler An asynchronous handler to be called once the module is undeployed.
   * @return The cluster client.
   */
  VertigoCluster undeployModule(String deploymentID, Handler<AsyncResult<Void>> doneHandler);

  /**
   * Undeploys a verticle from the cluster.
   *
   * @param deploymentID The unique verticle deployment ID.
   * @param doneHandler An asynchronous handler to be called once the verticle is undeployed.
   * @return The cluster client.
   */
  VertigoCluster undeployVerticle(String deploymentID, Handler<AsyncResult<Void>> doneHandler);

  /**
   * Returns an asynchronous cluster-wide replicated map.
   *
   * @param name The map name.
   * @return An asynchronous cluster-wide replicated map.
   */
  <K, V> WatchableAsyncMap<K, V> getMap(String name);

  /**
   * Returns an asynchronous cluster-wide replicated list.
   *
   * @param name The list name.
   * @return An asynchronous cluster-wide replicated list.
   */
  <T> AsyncList<T> getList(String name);

  /**
   * Returns an asynchronous cluster-wide replicated set.
   *
   * @param name The set name.
   * @return An asynchronous cluster-wide replicated set.
   */
  <T> AsyncSet<T> getSet(String name);

  /**
   * Returns an asynchronous cluster-wide replicated queue.
   *
   * @param name The queue name.
   * @return An asynchronous cluster-wide replicated queue.
   */
  <T> AsyncQueue<T> getQueue(String name);

  /**
   * Returns an asynchronous cluster-wide unique ID generator.
   *
   * @param name The ID generator name.
   * @return An asynchronous cluster-wide ID generator.
   */
  AsyncIdGenerator getIdGenerator(String name);

  /**
   * Returns an asynchronous cluster-wide lock.
   *
   * @param name The lock name.
   * @return An asynchronous cluster-wide lock.
   */
  AsyncLock getLock(String name);

}
