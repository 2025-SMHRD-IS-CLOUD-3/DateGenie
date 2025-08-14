package com.smhrd.db;

import java.io.IOException;
import java.io.Reader;
import java.util.ResourceBundle;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

public class SqlSessionManager {
	// 사용하는 DB의 기본 정보(driver, url, username, pw)를 가지고
	// 여러개의 sqlSession을 생성할 수 있는
	// sqlSessionFactory를 생성하고 관리하는 클래스
	// * sqlSession : DB에 접근할 수 있는 권한, 입장권
	public static SqlSessionFactory sqlSessionFactory;
	// 클래스 로딩 시(실행시), 딱 한번만 실행하고 더이상 실행 X
	static {
		// db의 정보가 담긴 xml파일 주소를 변수에 담아준다.
		// mybatis-config에 작성된 DB 주소를 가져와서 factory생성
		String resource = "com/smhrd/db/mybatis-config.xml";
		// reader를 사용해서 정보를 읽어와서 factory 생성
		try {
			Reader reader = Resources.getResourceAsReader(resource);
			// mybatis-config.xml에 담긴 정보를 reader를 통해 넘긴다.
			sqlSessionFactory = new SqlSessionFactoryBuilder().build(reader);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	// 다른 클래스에서 sqlSessionFactory를 가져오고 싶을 때, 사용할 메서드
	// 리턴값으로는 상단에 static 블럭에서 생성된 salSessionFactory 반환! 
	public static SqlSessionFactory getSqlSessionFactory() {
		return sqlSessionFactory;
	}
	
	
	
	
	
	
	
	
	
}
