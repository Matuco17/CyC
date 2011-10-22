package orco.security;


import orco.domain.Cliente;
import orco.domain.Responsable;

import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class OrcoUserDetailsService implements UserDetailsService {

	@Override
	public UserDetails loadUserByUsername(String username)
			throws UsernameNotFoundException, DataAccessException {
		
		//Primero busco si hay algun cliente con esto, sino sigo con otro tipo de usuario
		try {
			Cliente usuarioCliente = Cliente.findClientesByUserNamePortal(username).getSingleResult();
			return new OrcoUserDetails(usuarioCliente);
		} catch (EmptyResultDataAccessException e) {
			//Significa que no encontro ningun cliente con este parametro
		}
	
		
		//Busco por responsables
		try {
			Responsable usuarioResponsable = Responsable.findResponsablesByUsername(username).getSingleResult();
			return new OrcoUserDetails(usuarioResponsable);
		} catch (EmptyResultDataAccessException e) {
			//Significa que no encontro ningun responsable con este parametro
		}
	
		//Si no encuentro nada estonces devuelvo null
		throw new UsernameNotFoundException("No existe un usuario con ese nombre");
	}

}
