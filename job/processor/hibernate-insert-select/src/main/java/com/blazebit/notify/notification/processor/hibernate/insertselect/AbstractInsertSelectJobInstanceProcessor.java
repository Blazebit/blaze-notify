/*
 * Copyright 2018 Blazebit.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.blazebit.notify.notification.processor.hibernate.insertselect;

import com.blazebit.notify.job.JobException;
import com.blazebit.notify.job.JobInstance;
import com.blazebit.notify.job.JobInstanceProcessingContext;
import com.blazebit.notify.job.JobInstanceProcessor;
import com.blazebit.persistence.CriteriaBuilderFactory;
import com.blazebit.persistence.InsertCriteriaBuilder;

import javax.persistence.EntityManager;

public abstract class AbstractInsertSelectJobInstanceProcessor<ID, T, I extends JobInstance> implements JobInstanceProcessor<ID, I> {

    @Override
    public ID process(I jobInstance, JobInstanceProcessingContext<ID> context) {
        CriteriaBuilderFactory cbf = getCriteriaBuilderFactory(context);
        EntityManager em = getEntityManager(context);
        if (cbf == null) {
            throw new JobException("No CriteriaBuilderFactory given!");
        }
        if (em == null) {
            throw new JobException("No EntityManager given!");
        }
        InsertCriteriaBuilder<T> insertCriteriaBuilder = cbf.insert(em, getTargetEntityClass())
                .from(getJobInstanceEntityClass(), "jobInstance");

        insertCriteriaBuilder.where("jobInstance." + getJobInstanceIdPath()).eq(jobInstance.getId());
        insertCriteriaBuilder.setMaxResults(context.getProcessCount());

        bindTargetAttributes(insertCriteriaBuilder, jobInstance, context, "jobInstance");

        return execute(insertCriteriaBuilder, jobInstance, context);
    }

    protected EntityManager getEntityManager(JobInstanceProcessingContext<ID> context) {
        return context.getJobContext().getService(EntityManager.class);
    }

    protected CriteriaBuilderFactory getCriteriaBuilderFactory(JobInstanceProcessingContext<ID> context) {
        return context.getJobContext().getService(CriteriaBuilderFactory.class);
    }

    protected abstract void bindTargetAttributes(InsertCriteriaBuilder<T> insertCriteriaBuilder, I jobInstance, JobInstanceProcessingContext<ID> context, String jobInstanceAlias);

    protected abstract ID execute(InsertCriteriaBuilder<T> insertCriteriaBuilder, I jobInstance, JobInstanceProcessingContext<ID> context);

    protected abstract Class<T> getTargetEntityClass();

    protected abstract Class<I> getJobInstanceEntityClass();

    protected abstract String getJobInstanceIdPath();

}
