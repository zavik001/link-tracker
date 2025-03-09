package backend.academy.scrapper.entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;

@Data
public class ChatEntity {
    private final Long id;
    private final List<String> links = new ArrayList<>();
    private final Map<String, List<String>> filters = new HashMap<>();
    private final Map<String, List<String>> tags = new HashMap<>();
}
