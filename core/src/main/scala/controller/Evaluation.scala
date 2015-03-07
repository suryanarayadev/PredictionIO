/** Copyright 2015 TappingStone, Inc.
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

package io.prediction.controller

import io.prediction.core.BaseDataSource
import io.prediction.core.BasePreparator
import io.prediction.core.BaseAlgorithm
import io.prediction.core.BaseServing
import io.prediction.core.BaseEvaluator
import io.prediction.core.Doer
import io.prediction.core.BaseEngine
//import io.prediction.workflow.EngineWorkflow
import io.prediction.workflow.CreateWorkflow
import io.prediction.workflow.WorkflowUtils
import io.prediction.workflow.EngineLanguage
import io.prediction.workflow.PersistentModelManifest
import io.prediction.workflow.SparkWorkflowUtils
import io.prediction.workflow.StopAfterReadInterruption
import io.prediction.workflow.StopAfterPrepareInterruption
import io.prediction.data.storage.EngineInstance
import _root_.java.util.NoSuchElementException
import io.prediction.data.storage.StorageClientException

import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.rdd.RDD

import scala.language.implicitConversions

import org.json4s._
import org.json4s.native.JsonMethods._
import org.json4s.native.Serialization.read

import io.prediction.workflow.NameParamsSerializer
import grizzled.slf4j.Logger

/** Defines an evaluation that contains an engine and a metric.
  *
  * Implementations of this trait can be supplied to "pio eval" as the first
  * argument.
  *
  * @group Evaluation
  */
trait Evaluation extends Deployment {
  protected [this] var _evaluator: BaseEvaluator[_, _, _, _, _] = _
  protected [this] var evaluatorSet: Boolean = false

  def evaluator: BaseEvaluator[_, _, _, _, _] = {
    assert(evaluatorSet, "Evaluator not set")
    _evaluator
  }

  /** Returns both the [[Engine]] and [[Metric]] contained in this
    * [[Evaluation]].
    */
  def engineMetric: (BaseEngine[_, _, _, _], Metric[_, _, _, _, _]) = {
    throw new NotImplementedError("You should not call this")
    (engine, null.asInstanceOf[Metric[_,_,_,_,_]])
  }

  /** Sets both the [[Engine]] and [[Metric]] for this [[Evaluation]]. */
  def engineMetric_=[EI, Q, P, A](
    engineMetric: (BaseEngine[EI, Q, P, A], Metric[EI, Q, P, A, _])) {
    assert(!evaluatorSet, "Evaluator can be set at most once")
    //_engine = engineMetric._1
    engine = engineMetric._1
    _evaluator = new MetricEvaluator(engineMetric._2)
    evaluatorSet = true
  }

  def engineEvaluator
  : (BaseEngine[_, _, _, _], BaseEvaluator[_, _, _, _, _]) = {
    assert(evaluatorSet, "Evaluator not set")
    (engine, _evaluator)
  }

  def engineEvaluator_=[EI, Q, P, A](
    engineEvaluator: (BaseEngine[EI, Q, P, A], BaseEvaluator[EI, Q, P, A, _])) {
    assert(!evaluatorSet, "Evaluator can be set at most once")
    engine = engineEvaluator._1
    _evaluator = engineEvaluator._2
    evaluatorSet = true
  }
}
