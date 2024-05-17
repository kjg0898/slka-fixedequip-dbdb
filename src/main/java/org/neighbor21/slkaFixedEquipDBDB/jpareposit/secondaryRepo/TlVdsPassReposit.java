package org.neighbor21.slkaFixedEquipDBDB.jpareposit.secondaryRepo;

import org.neighbor21.slkaFixedEquipDBDB.entity.compositekey.TL_VDS_PASSKey;
import org.neighbor21.slkaFixedEquipDBDB.entity.secondary.TL_VDS_PASS;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * packageName    : org.neighbor21.slkafixedequipdbdb.jpareposit
 * fileName       : TlVdsPassReposit.java
 * author         : kjg08
 * date           : 24. 4. 8.
 * description    :  TL_VDS_PASS 엔티티에 대한 기본적인 CRUD 연산을 수행할 수 있게 해줍니다. @Repository 애너테이션은 이 인터페이스가 데이터 액세스 계층의 컴포넌트임을 나타냅니다. Spring Data JPA는 이 인터페이스의 구현체를 런타임에 자동으로 생성하여, 직접 구현할 필요 없이 바로 사용할 수 있게 해줍니다
 * ===========================================================
 * DATE              AUTHOR             NOTE
 * -----------------------------------------------------------
 * 24. 4. 8.        kjg08           최초 생성
 */

@Repository
public interface TlVdsPassReposit extends JpaRepository<TL_VDS_PASS, TL_VDS_PASSKey> {

}
