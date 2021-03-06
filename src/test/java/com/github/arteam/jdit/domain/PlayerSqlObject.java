package com.github.arteam.jdit.domain;

import com.github.arteam.jdit.domain.entity.Player;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableSet;
import org.joda.time.DateTime;
import org.skife.jdbi.v2.SQLStatement;
import org.skife.jdbi.v2.StatementContext;
import org.skife.jdbi.v2.sqlobject.*;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapper;
import org.skife.jdbi.v2.sqlobject.customizers.SingleValueResult;
import org.skife.jdbi.v2.tweak.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Date: 1/25/15
 * Time: 11:26 PM
 *
 * @author Artem Prigoda
 */
@RegisterMapper(PlayerSqlObject.PlayerMapper.class)
public interface PlayerSqlObject {

    @GetGeneratedKeys
    @SqlUpdate("insert into players(first_name, last_name, birth_date, weight, height) values" +
            "(:first_name, :last_name, :birth_date, :weight, :height)")
    Long createPlayer(@Bind("first_name") String firstName, @Bind("last_name") String lastName,
                      @Bind("birth_date") Date birthDate, @Bind("height") int height,
                      @Bind("weight") int weight);

    @GetGeneratedKeys
    @SqlUpdate("insert into players(first_name, last_name, birth_date, weight, height) values" +
            "(:first_name, :last_name, :birth_date, :weight, :height)")
    Long createPlayer(@Bind(binder = PlayerBinder.class) Player player);

    @SqlQuery("select last_name from players order by last_name")
    List<String> getLastNames();

    @SqlQuery("select count(*) from players where extract(year from birth_date) = :year")
    int getAmountPlayersBornInYear(@Bind("year") int year);

    @SqlQuery("select * from players where birth_date > :date")
    List<Player> getPlayersBornAfter(@Bind("date") DateTime date);

    @SqlQuery("select birth_date from players where first_name=:first_name and last_name=:last_name")
    DateTime getPlayerBirthDate(@Bind("first_name") String firstName, @Bind("last_name") String lastName);

    @SqlQuery("select distinct extract(year from birth_date) player_year from players order by player_year")
    Set<Integer> getBornYears();

    @SqlQuery("select * from players where first_name=:first_name and last_name=:last_name")
    @SingleValueResult
    Optional<Player> findPlayer(@Bind("first_name") String firstName, @Bind("last_name") String lastName);

    @SqlQuery("select * from players where weight is not distinct from :weight")
    List<Player> getPlayersByWeight(@Bind("weight") Optional<Integer> weight);

    @SqlQuery("select first_name from players")
    ImmutableSet<String> getFirstNames();

    class PlayerBinder implements Binder<Bind, Player> {

        @Override
        public void bind(SQLStatement<?> q, Bind bind, Player p) {
            q.bind("first_name", p.firstName);
            q.bind("last_name", p.lastName);
            q.bind("birth_date", p.birthDate);
            q.bind("weight", p.weight);
            q.bind("height", p.height);
        }
    }

    class PlayerMapper implements ResultSetMapper<Player> {
        @Override
        public Player map(int index, ResultSet r, StatementContext ctx) throws SQLException {
            int height = r.getInt("height");
            int weight = r.getInt("weight");
            return new Player(Optional.of(r.getLong("id")), r.getString("first_name"), r.getString("last_name"),
                    r.getTimestamp("birth_date"),
                    height != 0 ? Optional.of(height) : Optional.<Integer>absent(),
                    weight != 0 ? Optional.of(weight) : Optional.<Integer>absent());
        }
    }
}
