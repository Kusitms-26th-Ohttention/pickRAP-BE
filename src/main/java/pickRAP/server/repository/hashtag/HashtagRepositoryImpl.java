package pickRAP.server.repository.scrap;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class HashTagRepositoryImpl implements HashTagRepositoryCustom{

    private final JPAQueryFactory jpaQueryFactory;




}
