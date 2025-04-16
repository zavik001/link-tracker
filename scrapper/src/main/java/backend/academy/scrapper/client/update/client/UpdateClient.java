package backend.academy.scrapper.client.update.client;

import backend.academy.scrapper.dto.UpdateResponse;

public interface UpdateClient {
    void sendUpdate(UpdateResponse updateResponse);
}
