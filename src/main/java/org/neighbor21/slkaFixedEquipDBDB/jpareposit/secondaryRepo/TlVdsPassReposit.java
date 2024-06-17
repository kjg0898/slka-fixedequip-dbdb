package org.neighbor21.slkaFixedEquipDBDB.jpareposit.secondaryRepo;

import org.neighbor21.slkaFixedEquipDBDB.entity.compositekey.TL_VDS_PASSKey;
import org.neighbor21.slkaFixedEquipDBDB.entity.secondary.TL_VDS_PASS;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Set;

/**
 * packageName    : org.neighbor21.slkafixedequipdbdb.jpareposit.secondaryRepo
 * fileName       : TlVdsPassReposit.java
 * author         : kjg08
 * date           : 24. 4. 8.
 * description    : TL_VDS_PASS 엔티티에 대한 기본적인 CRUD 연산을 수행할 수 있게 해줍니다.
 * <p>
 * Repository 애너테이션은 이 인터페이스가 데이터 액세스 계층의 컴포넌트임을 나타냅니다. Spring Data JPA는 이 인터페이스의 구현체를 런타임에 자동으로 생성하여, 직접 구현할 필요 없이 바로 사용할 수 있게 해줍니다.
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 24. 4. 8.        kjg08           최초 생성
 */

@Repository
public interface TlVdsPassReposit extends JpaRepository<TL_VDS_PASS, TL_VDS_PASSKey> {

    /**
     * 주어진 키 세트에 해당하는 존재하는 키들을 조회하는 메소드.
     *
     * @param keys 조회할 키 세트
     * @return 존재하는 키 세트
     */
    @Query("SELECT t.tlVdsPassPK FROM TL_VDS_PASS t WHERE t.tlVdsPassPK IN :keys")
    Set<TL_VDS_PASSKey> findExistingKeys(@Param("keys") Set<TL_VDS_PASSKey> keys);
}