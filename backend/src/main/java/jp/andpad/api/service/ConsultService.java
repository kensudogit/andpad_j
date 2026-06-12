package jp.andpad.api.service;

import java.util.List;

import org.springframework.stereotype.Service;

import jp.andpad.api.domain.ExtendedTypes.ConsultMessage;
import jp.andpad.api.domain.ExtendedTypes.ConsultMessageReply;
import jp.andpad.api.domain.ExtendedTypes.ConsultThread;
import jp.andpad.api.domain.ExtendedTypes.RagAnswer;
import jp.andpad.api.domain.ExtendedTypes.RagDocument;
import jp.andpad.api.domain.ExtendedTypes.RagSearchHit;
import jp.andpad.api.graphql.input.LearningInputs.CreateRagDocumentInput;
import jp.andpad.api.repository.ConsultRagRepository;
import jp.andpad.api.security.AuthPrincipal;
import jp.andpad.api.security.TenantContext;
import jp.andpad.api.util.Dates;
import jp.andpad.api.util.RagHelper;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ConsultService {

    private final ConsultRagRepository consultRagRepository;

    public List<ConsultThread> listThreads() {
        AuthPrincipal p = TenantContext.requirePrincipal();
        return consultRagRepository.listThreads(p.orgId(), p.userId(), isOrgWide(p));
    }

    public ConsultThread getThread(String id) {
        AuthPrincipal p = TenantContext.requirePrincipal();
        return consultRagRepository.getThread(p.orgId(), p.userId(), id, isOrgWide(p));
    }

    public ConsultMessageReply sendMessage(String threadId, String message) {
        AuthPrincipal p = TenantContext.requirePrincipal();
        String orgId = p.orgId();
        String userId = p.userId();
        String tid = threadId;
        if (tid == null || tid.isBlank()) {
            String title = truncate(message, 40);
            if (title.isBlank()) {
                title = "相談スレッド";
            }
            tid = consultRagRepository.createThread(orgId, userId, title).id();
        } else if (!consultRagRepository.verifyThreadAccess(orgId, userId, tid)) {
            throw new IllegalArgumentException("thread not found or access denied");
        }
        ConsultMessage userMessage = consultRagRepository.addMessage(orgId, tid, "user", message);
        List<ConsultMessage> history = consultRagRepository.listMessages(orgId, tid);
        String reply = generateReply(message, history);
        ConsultMessage assistantMessage = consultRagRepository.addMessage(orgId, tid, "assistant", reply);
        consultRagRepository.incrementConsultUsage(orgId, message.length() + reply.length());
        return new ConsultMessageReply(tid, userMessage, assistantMessage);
    }

    public List<RagDocument> listRagDocuments() {
        return consultRagRepository.listRagDocuments(TenantContext.orgId());
    }

    public RagDocument createRagDocument(CreateRagDocumentInput input) {
        return consultRagRepository.createRagDocument(TenantContext.orgId(), input);
    }

    public List<RagSearchHit> searchRag(String query, int limit) {
        String orgId = TenantContext.orgId();
        List<RagSearchHit> hits = consultRagRepository.searchRagDocuments(orgId, query, limit);
        if (!hits.isEmpty()) {
            return hits;
        }
        return RagHelper.localSearch(consultRagRepository.listRagDocuments(orgId), query, limit);
    }

    public RagAnswer ragAnswer(String query) {
        List<RagSearchHit> hits = searchRag(query, 5);
        return new RagAnswer(RagHelper.formatHitsAsAnswer(hits), hits);
    }

    private String generateReply(String message, List<ConsultMessage> history) {
        String topic = truncate(message, 40);
        if (topic.isBlank()) {
            topic = "（質問内容）";
        }
        return """
                現在 OpenAI 連携（OPENAI_API_KEY）が未設定のため、AI による詳細回答を生成できません。

                Railway の andpad サービス → Variables → OPENAI_API_KEY を追加し Redeploy すると、\
                ご質問「%s」に対する本格的な回答が得られます。

                ※ デモ応答として受け付けました。建設現場の安全・工程・品質管理などについてお気軽にご質問ください。"""
                .formatted(topic);
    }

    private static boolean isOrgWide(AuthPrincipal p) {
        return "OWNER".equals(p.role()) || "ADMIN".equals(p.role());
    }

    private static String truncate(String s, int max) {
        if (s == null) {
            return "";
        }
        String t = s.trim();
        return t.length() <= max ? t : t.substring(0, max);
    }
}
