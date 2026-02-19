package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import ru.yandex.practicum.filmorate.dao.MpaDbStorage;
import ru.yandex.practicum.filmorate.mapper.MpaRowMapper;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@JdbcTest
@AutoConfigureTestDatabase
@Import({MpaDbStorage.class, MpaRowMapper.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class MpaDbStorageTest {

    private final MpaDbStorage mpaDbStorage;
    private final JdbcTemplate jdbcTemplate;

    @Test
    void getAllMpaTest() {
        List<Mpa> mpaFromDb = jdbcTemplate.query(
                "SELECT * FROM rating ORDER BY rating_id",
                (rs, rowNum) -> {
                    Mpa mpa = new Mpa();
                    mpa.setId(rs.getLong("rating_id"));
                    mpa.setName(rs.getString("name"));
                    return mpa;
                });
        assertEquals(5, mpaFromDb.size(), "В таблице rating должно быть 5 записей");
    }

    @Test
    void getByIdTest() {
        Optional<Mpa> mpaOptional = mpaDbStorage.getById(1L);
        Mpa mpa = mpaOptional.get();
        assertEquals(1L, mpa.getId());
        assertEquals("G", mpa.getName());
    }
}
