package io.legacyfighter.cabs.contracts.application.editor;

import io.legacyfighter.cabs.contracts.model.content.DocumentContent;
import io.legacyfighter.cabs.contracts.model.content.DocumentContentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class DocumentEditor {

    private final DocumentContentRepository documentContentRepository;

    public DocumentEditor(DocumentContentRepository documentContentRepository) {
        this.documentContentRepository = documentContentRepository;
    }

    @Transactional
    public CommitResult commit(DocumentDTO document) {
        UUID previousID = document.getContentId();

        DocumentContent content = new DocumentContent(
                previousID,
                document.getDocumentVersion(),
                document.getPhysicalContent()
        );

        documentContentRepository.save(content);

        return new CommitResult(content.getId(), CommitResult.Result.SUCCESS);
    }


    @Transactional
    public DocumentDTO get(UUID contentId) {
        DocumentContent content = documentContentRepository.getOne(contentId);

        return new DocumentDTO(
                contentId,
                content.getPhysicalContent(),
                content.getDocumentVersion()
        );
    }
}
