package app.foot.repository;

import app.foot.repository.entity.TeamEntity;

public interface TeamRepository {
    TeamEntity findByName(String name);
}
