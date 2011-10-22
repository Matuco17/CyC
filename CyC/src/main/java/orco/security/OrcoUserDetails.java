package orco.security;

import java.util.ArrayList;
import java.util.Collection;

import orco.domain.Cliente;
import orco.domain.Responsable;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;


public class OrcoUserDetails implements UserDetails {

	private static final long serialVersionUID = 977843032482098347L;

	private Responsable usuarioResponsable;
	private Cliente usuarioCliente;
	
	public OrcoUserDetails(Responsable responsable){
		usuarioResponsable = responsable;
		usuarioCliente = null;
	}
	
	public OrcoUserDetails(Cliente cliente){
		usuarioResponsable = null;
		usuarioCliente = cliente;
	}
	
	
	@Override
	public Collection<GrantedAuthority> getAuthorities() {
		Collection<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();
		if (usuarioCliente != null){
			authorities.add(new OrcoGrantedAuthority(OrcoGrantedAuthority.ROLE_CLIENTE));	
			authorities.add(new OrcoGrantedAuthority(OrcoGrantedAuthority.ROLE_USUARIO_SISTEMA));	
		} else if (usuarioResponsable != null){
			if (usuarioResponsable.getEsAdministrativo()){
				authorities.add(new OrcoGrantedAuthority(OrcoGrantedAuthority.ROLE_ADMIN));			
			}
			if (usuarioResponsable.getEsOperario()){
				authorities.add(new OrcoGrantedAuthority(OrcoGrantedAuthority.ROLE_OPERARIO));			
			}
			authorities.add(new OrcoGrantedAuthority(OrcoGrantedAuthority.ROLE_USUARIO_INTERNO));	
			authorities.add(new OrcoGrantedAuthority(OrcoGrantedAuthority.ROLE_USUARIO_SISTEMA));	
		}
		return authorities;
	}

	@Override
	public String getPassword() {
		if (usuarioCliente != null){
			return usuarioCliente.getPasswordPortal();
		} else if (usuarioResponsable != null){
			return usuarioResponsable.getPassword();
		}
		return "r0ncu0q394uhw0e9ruhnisadufpuwe0f984yrqwud9q8wdny9q"; //Password indecifrable
	}

	@Override
	public String getUsername() {
		if (usuarioCliente != null){
			return usuarioCliente.getUserNamePortal();
		} else if (usuarioResponsable !=  null){
			return usuarioResponsable.getUsername();
		}
		return null;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

}
