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

package com.blazebit.notify.job.jpa.model;

import javax.persistence.*;
import java.io.Serializable;

@MappedSuperclass
public abstract class BaseEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long id;
	private Long version;

	public BaseEntity() {
	}

	public BaseEntity(Long id) {
		this.id = id;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "idGenerator")
	public Long getId() {
		return id;
	}
	
	public void setId(Long id){
		this.id = id;
	}

	@Version
	@Basic(optional = false)
	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getNoProxyClass(getClass()) != getNoProxyClass(obj.getClass()))
			return false;
		BaseEntity other = (BaseEntity) obj;
		// null does not equal null in case of ids!
		if (id == null || other.id == null) {
			return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Transient
	public Class<?> getEntityClass() {
		return getNoProxyClass(getClass());
	}

	protected static Class<?> getNoProxyClass(Class<?> clazz) {
		while (clazz.getName().contains("javassist")) {
			clazz = clazz.getSuperclass();
		}
		
		return clazz;
	}
}
