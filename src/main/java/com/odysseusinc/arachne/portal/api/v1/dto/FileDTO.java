/*
 *
 * Copyright 2017 Observational Health Data Sciences and Informatics
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
 * Authors: Pavel Grafkin, Alexandr Ryabokon, Vitaly Koulakov, Anton Gackovka, Maria Pozhidaeva, Mikhail Mironov
 * Created: January 13, 2017
 *
 */

package com.odysseusinc.arachne.portal.api.v1.dto;

import java.util.Date;

public class FileDTO {
    protected String uuid;
    protected String name;
    protected String label;
    protected Date created;
    protected Date updated;
    protected String docType;
    protected String mimeType;
    protected UserInfoDTO author;
    protected String content;

    public String getContent() {

        return content;
    }

    public void setContent(String content) {

        this.content = content;
    }

    public FileDTO() {

    }

    public FileDTO(String uuid, String name, String label, Date created, Date updated, String docType, String mimeType, UserInfoDTO author) {

        this(name, label, created, updated, docType, mimeType, author);
        this.uuid = uuid;
    }

    public FileDTO(String name, String label, Date created, Date updated, String docType, String mimeType, UserInfoDTO author) {

        this.name = name;
        this.label = label;
        this.created = created;
        this.updated = updated;
        this.docType = docType;
        this.mimeType = mimeType;
        this.author = author;
    }

    public String getUuid() {

        return uuid;
    }

    public void setUuid(String uuid) {

        this.uuid = uuid;
    }

    public String getLabel() {

        return label;
    }

    public void setLabel(String label) {

        this.label = label;
    }

    public Date getCreated() {

        return created;
    }

    public void setCreated(Date created) {

        this.created = created;
    }

    public Date getUpdated() {

        return updated;
    }

    public void setUpdated(Date updated) {

        this.updated = updated;
    }

    public String getDocType() {

        return docType;
    }

    public void setDocType(String docType) {

        this.docType = docType;
    }

    public String getMimeType() {

        return mimeType;
    }

    @Deprecated
    public void setMimeType(String mimeType) {

        this.mimeType = mimeType;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public UserInfoDTO getAuthor() {

        return author;
    }

    public void setAuthor(UserInfoDTO author) {

        this.author = author;
    }
}
