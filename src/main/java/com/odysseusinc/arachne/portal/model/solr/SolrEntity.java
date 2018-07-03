/*
 * Copyright 2018 Odysseus Data Services, inc.
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
 *
 * Company: Odysseus Data Services, Inc.
 * Product Owner/Architecture: Gregory Klebanov
 * Authors: Anton Gackovka
 * Created: February 26, 2018
 */

package com.odysseusinc.arachne.portal.model.solr;


import com.odysseusinc.arachne.portal.model.Identifiable;

public interface SolrEntity extends Identifiable {

    String ID_DELIMITER = "-";

    SolrCollection getCollection();


    default String getSolrId() {

        return getId() + ID_DELIMITER + getCollection().getName();
    }
}
