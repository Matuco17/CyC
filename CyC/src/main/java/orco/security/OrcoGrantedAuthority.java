package orco.security;

import org.springframework.security.core.GrantedAuthority;

public class OrcoGrantedAuthority implements GrantedAuthority {

	private static final long serialVersionUID = 1054962397654634568L;
	
	public static String ROLE_ADMIN = "ORCO_ROLE_ADMINISTRATIVO";
	public static String ROLE_OPERARIO = "ORCO_ROLE_OPERARIO";
	public static String ROLE_CLIENTE = "ORCO_ROLE_CLIENTE";
	public static String ROLE_USUARIO_INTERNO = "ORCO_USUARIO_INTERNO";
	public static String ROLE_USUARIO_SISTEMA = "ORCO_USUARIO_SISTEMA";
	
	private String actualAuthority = null;
	
	public OrcoGrantedAuthority(String authority){
		actualAuthority = authority;
	}
	 
	
	@Override
	public String getAuthority() {
		return actualAuthority;
	}


}
