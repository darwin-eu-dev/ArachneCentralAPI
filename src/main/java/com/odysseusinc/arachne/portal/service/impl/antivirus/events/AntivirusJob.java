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
 * Authors: Pavel Grafkin, Alexandr Ryabokon, Vitaly Koulakov, Anton Gackovka, Maria Pozhidaeva, Konstantin Yaroshovets
 * Created: January 22, 2017
 *
 */

package com.odysseusinc.arachne.portal.service.impl.antivirus.events;

import java.io.InputStream;

public class AntivirusJob {
    private final Long fileId;
    private final String fileName;
    private final InputStream content;
    private final AntivirusJobFileType antivirusJobFileType;

    public AntivirusJob(Long fileId, String fileName, InputStream content, AntivirusJobFileType antivirusJobFileType) {

        this.fileId = fileId;
        this.fileName = fileName;
        this.content = content;
        this.antivirusJobFileType = antivirusJobFileType;
    }

    public Long getFileId() {

        return fileId;
    }

    public String getFileName() {

        return fileName;
    }

    public InputStream getContent() {

        return content;
    }

    public AntivirusJobFileType getAntivirusJobFileType() {

        return antivirusJobFileType;
    }
}
