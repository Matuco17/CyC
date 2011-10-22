//Funcion que actualiza todos los totales de la grilla
function actualizarTotales(){
	var cantidades = document.getElementsByName('cantidad');
	var preciosUnitarios = document.getElementsByName('precioUnitario');
	var preciosTotales = document.getElementsByName('precioTotal');
	var subtotalPresupuesto = document.getElementById('subtotal');
	var porcImpuesto = document.getElementById("porcImpuesto");
	var porcBonificacion = document.getElementById("porcBonificacion");
	var subtotalImpuesto = document.getElementById('subtotalImpuesto');
	var totalPresupuesto = document.getElementById('total');
	
	var totalAux = 0;
	var cantidadFilas = cantidades.length;
	var multiplicadorBonificacion = 1;
	
	if((porcBonificacion != null) && !isNaN(parseFloat(porcBonificacion.value))){
		multiplicadorBonificacion = 1 - (parseFloat(porcBonificacion.value) / 100);
	}

	for(i = 0; i < cantidadFilas; i++){
		var subTotal = parseFloat(cantidades[i].value) * parseFloat(preciosUnitarios[i].value);
		if (!isNaN(subTotal)){
			preciosTotales[i].value = roundNumber(subTotal, 2);
			totalAux = totalAux + subTotal;
		}			
	}
	totalAux = totalAux * multiplicadorBonificacion;
	
	subtotalPresupuesto.value = roundNumber(totalAux, 2) ; //Paso el resultado pero formateado
	
	if (!isNaN(parseFloat(porcImpuesto.value))){
		subtotalImpuesto.value = roundNumber(totalAux * parseFloat(porcImpuesto.value) / 100, 2);
		totalAux = totalAux + (totalAux * parseFloat(porcImpuesto.value) / 100);
	} else {
		subtotalImpuesto.value = "0.00";
	}
	
	totalPresupuesto.value = roundNumber(totalAux, 2);
	
}


function roundNumber(num, dec) {
	var result = Math.round(num*Math.pow(10,dec))/Math.pow(10,dec);
	return result;
}
