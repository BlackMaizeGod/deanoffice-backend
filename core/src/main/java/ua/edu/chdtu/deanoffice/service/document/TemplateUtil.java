package ua.edu.chdtu.deanoffice.service.document;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.exceptions.Docx4JException;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.JaxbXmlPart;
import org.docx4j.openpackaging.parts.relationships.Namespaces;
import org.docx4j.openpackaging.parts.relationships.RelationshipsPart;
import org.docx4j.relationships.Relationship;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Text;
import org.docx4j.wml.Tr;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import javax.xml.bind.JAXBElement;
import java.util.*;
import java.util.stream.Collectors;

public class TemplateUtil {

    private static Logger log = LoggerFactory.getLogger(TemplateUtil.class);
    public static final String PLACEHOLDER_PREFIX = "#";

    public static List<Object> getAllElementsFromObject(Object obj, Class<?> toSearch) {
        List<Object> result = new ArrayList<>();
        if (obj instanceof JAXBElement) {
            obj = ((JAXBElement<?>) obj).getValue();
        }

        if (obj.getClass().equals(toSearch)) {
            result.add(obj);
        } else {
            if (obj instanceof ContentAccessor) {
                List<?> children = ((ContentAccessor) obj).getContent();
                for (Object child : children) {
                    result.addAll(getAllElementsFromObject(child, toSearch));
                }
            }
        }
        return result;
    }

    public static void replaceTextPlaceholdersInTemplate(WordprocessingMLPackage template, Map<String, String> placeholdersValues) {
        List<Text> placeholders = getTextsContainingPlaceholders(template);
        replaceValuesInTextPlaceholders(placeholders, placeholdersValues);
    }

    private static void replaceValuesInTextPlaceholders(List<Text> placeholders, Map<String, String> replacements) {
        for (Text text : placeholders) {
            String replacement = replacements.get(text.getValue().trim().replaceFirst(PLACEHOLDER_PREFIX, ""));
            if (StringUtils.isEmpty(replacement)) {
                log.warn("{} is empty", text.getValue());
            } else {
                text.setValue(replacement);
            }
        }
    }

    private static List<Text> getTextsContainingPlaceholders(WordprocessingMLPackage template) {
        List<Object> texts = getAllElementsFromObject(template.getMainDocumentPart(), Text.class);
        List<Object> placeholders = new ArrayList<>();
        for (int i = 0; i < texts.size(); i++) {
            Text text = ((Text) texts.get(i));
            if (isAPlaceholder(text)) {
                placeholders.add(text);
                continue;
            }
            if (text.getValue().trim().equals(PLACEHOLDER_PREFIX)) {
                Iterator<Object> iterator = texts.listIterator(i + 1);
                if (iterator.hasNext()) {
                    Text potentialPlaceholder = (Text) iterator.next();
                    potentialPlaceholder.setValue(text.getValue() + potentialPlaceholder.getValue());
                    text.setValue("");
                    placeholders.add(potentialPlaceholder);
                    i++;
                }
            }
        }
        List<Text> result = placeholders.stream().map(o -> (Text) o).collect(Collectors.toList());
        return result;
    }

    public static void replacePlaceholdersWithBlank(WordprocessingMLPackage template, Set<String> placeholders) {
        List<Text> texts = getTextsContainingPlaceholders(template);
        for (Text text : texts) {
            if (placeholders.contains(text.getValue())) {
                text.setValue("");
            }
        }
    }

    private static void replacePlaceholdersInRelativeElement(WordprocessingMLPackage template,
                                                             String relationType,
                                                             Map<String, String> dictionary) throws Docx4JException {
        RelationshipsPart relationshipPart = template.getMainDocumentPart().getRelationshipsPart();
        List<Relationship> relationships = relationshipPart.getRelationshipsByType(relationType);
        List<Text> texts = new ArrayList<>();
        for (Relationship r : relationships) {
            JaxbXmlPart part = (JaxbXmlPart) relationshipPart.getPart(r);
            List<Object> textObjects = getAllElementsFromObject(part.getContents(), Text.class);
            for (Object textObject : textObjects) {
                Text text = (Text) textObject;
                if (isAPlaceholder(text)) {
                    texts.add(text);
                }
            }
        }
        replaceValuesInTextPlaceholders(texts, dictionary);
    }

    public static void replacePlaceholdersInFooter(WordprocessingMLPackage template, Map<String, String> dictionary)
            throws Docx4JException {
        replacePlaceholdersInRelativeElement(template, Namespaces.FOOTER, dictionary);
    }

    private static boolean isAPlaceholder(Text text) {
        return text.getValue() != null && text.getValue().startsWith(PLACEHOLDER_PREFIX) && text.getValue().length() > 1;
    }

    public static Tr findRowInTable(Tbl table, String templateKey) {
        for (Object row : table.getContent()) {
            List<?> textElements = getAllElementsFromObject(row, Text.class);
            for (Object text : textElements) {
                Text textElement = (Text) text;
                if (textElement.getValue() != null && textElement.getValue().trim().equals(templateKey)) {
                    return (Tr) row;
                }
            }
        }
        return null;
    }

    public static Tbl findTable(List<Object> tables, String templateKey) {
        for (Object tbl : tables) {
            List<Object> textElements = getAllElementsFromObject(tbl, Text.class);
            for (Object text : textElements) {
                Text textElement = (Text) text;
                if (textElement.getValue() != null && textElement.getValue().trim().equals(templateKey)) {
                    return (Tbl) tbl;
                }
            }
        }
        return null;
    }

    public static void addRowToTable(Tbl reviewTable, Tr templateRow, int rowNumber, Map<String, String> replacements) {
        Tr workingRow = XmlUtils.deepCopy(templateRow);
        List<?> textElements = getAllElementsFromObject(workingRow, Text.class);
        for (Object object : textElements) {
            Text text = (Text) object;
            String replacementValue = replacements.get(text.getValue().trim().replaceFirst(PLACEHOLDER_PREFIX, ""));
            if (replacementValue != null) {
                text.setValue(replacementValue);
            }
        }
        reviewTable.getContent().add(rowNumber, workingRow);
    }

    public static String getValueSafely(String value, String ifNullOrEmpty) {
        return StringUtils.isEmpty(value) ? ifNullOrEmpty : value;
    }

    public static String getValueSafely(String value) {
        return getValueSafely(value, "");
    }
}