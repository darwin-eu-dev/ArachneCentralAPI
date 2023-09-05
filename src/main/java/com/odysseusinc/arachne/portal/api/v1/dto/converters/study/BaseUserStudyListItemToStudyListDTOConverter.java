/*
 *
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
 * Authors: Pavel Grafkin, Alexandr Ryabokon, Vitaly Koulakov, Anton Gackovka, Maria Pozhidaeva, Mikhail Mironov
 * Created: September 11, 2017
 *
 */

package com.odysseusinc.arachne.portal.api.v1.dto.converters.study;

import com.odysseusinc.arachne.portal.api.v1.dto.PermissionsDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.ShortUserDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.StudyListDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.converters.BaseConversionServiceAwareConverter;
import com.odysseusinc.arachne.portal.api.v1.dto.dictionary.StudyStatusDTO;
import com.odysseusinc.arachne.portal.api.v1.dto.dictionary.StudyTypeDTO;
import com.odysseusinc.arachne.portal.model.AbstractUserStudyListItem;
import com.odysseusinc.arachne.portal.model.Analysis;
import com.odysseusinc.arachne.portal.model.DataNode;
import com.odysseusinc.arachne.portal.model.DataNodeUser;
import com.odysseusinc.arachne.portal.model.IUser;
import com.odysseusinc.arachne.portal.model.ParticipantRole;
import com.odysseusinc.arachne.portal.model.Study;
import com.odysseusinc.arachne.portal.model.Submission;
import com.odysseusinc.arachne.portal.service.StudyService;

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.odysseusinc.arachne.portal.model.SubmissionStatus.IN_PROGRESS;
import static com.odysseusinc.arachne.portal.model.SubmissionStatus.PENDING;
import static com.odysseusinc.arachne.portal.model.SubmissionStatus.QUEUE_PROCESSING;
import static com.odysseusinc.arachne.portal.model.SubmissionStatus.STARTING;

@Component
public class BaseUserStudyListItemToStudyListDTOConverter extends BaseConversionServiceAwareConverter<AbstractUserStudyListItem, StudyListDTO> {

    @Autowired
    private StudyService studyService;

    @Override
    public StudyListDTO convert(AbstractUserStudyListItem userStudyLink) {
        StudyListDTO studyDTO = createResultObject();
        Study source = userStudyLink.getStudy();
        List<IUser> studyLeadList = studyService.findLeads(source);

        studyDTO.setStatus(conversionService.convert(source.getStatus(), StudyStatusDTO.class));
        studyDTO.setTitle(source.getTitle());
        studyDTO.setType(conversionService.convert(source.getType(), StudyTypeDTO.class));
        studyDTO.setEndDate(source.getEndDate());
        studyDTO.setStartDate(source.getStartDate());
        studyDTO.setDescription(source.getDescription());
        studyDTO.setUpdated(source.getUpdated());
        studyDTO.setCreated(source.getCreated());
        studyDTO.setId(source.getId());
        StringBuilder stringBuilder = new StringBuilder();
        for (String participantRole : userStudyLink.getRole() != null
                ? userStudyLink.getRole().split(",")
                : new String[]{}) {
            stringBuilder.append(ParticipantRole.valueOf(participantRole)).append(", ");
        }
        if (stringBuilder.length() > 1) {
            stringBuilder.delete(stringBuilder.length() - 2, stringBuilder.length());
            studyDTO.setRole(stringBuilder.toString());
        }
        studyDTO.setPermissions(conversionService.convert(source, PermissionsDTO.class));
        studyDTO.setFavourite(userStudyLink.getFavourite());
        studyDTO.setLeadList(
                studyLeadList.stream()
                        .map(studyLead -> conversionService.convert(studyLead, ShortUserDTO.class))
                        .collect(Collectors.toList())
        );
        studyDTO.setPrivacy(source.getPrivacy());
        studyDTO.setActionRequired(isActionRequired(userStudyLink));
        proceedAdditionalFields(studyDTO, userStudyLink);

        return studyDTO;
    }

    @Override
    protected StudyListDTO createResultObject() {

        return new StudyListDTO();
    }
    
    private boolean isActionRequired(AbstractUserStudyListItem userStudyLink){
        List<DataNode> usersNodes = userStudyLink
                .getUser()
                .getDataNodeUsers()
                .stream()
                .map(DataNodeUser::getDataNode)
                .collect(Collectors.toList());

        return userStudyLink
                .getStudy()
                .getAnalyses()
                .stream()
                .map(Analysis::getSubmissions)
                .flatMap(Collection::stream)
                .filter(s -> usersNodes.contains(s.getDataSource().getDataNode()))
                .map(Submission::getStatus)
                .anyMatch(st -> EnumSet.of(PENDING, STARTING, IN_PROGRESS, QUEUE_PROCESSING).contains(st));
    }

}
