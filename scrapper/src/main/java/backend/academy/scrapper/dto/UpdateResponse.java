package backend.academy.scrapper.dto;

import java.util.List;

public record UpdateResponse(Long id, String url, String update, List<Long> chatIds) {}
