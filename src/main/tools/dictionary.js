Ext.define('hiro.Field', {

	name: null,
	type: null,
	notnull: false,
	json: true,
	maxlength: null,
	dflt: null,
	colname: null,
	source: null,
	label: null,
	description: null,
	sample: null,
	
	constructor: function(config)
	{
		Ext.apply(this,config);
		Ext.applyIf(this,
		{
			label: this.name
		});
		if (!isNaN(this.maxlength))
			this.maxlength=parseInt(this.maxlength,10);
		this.notnull=(this.notnull==='TRUE');
		if (this.colname==null || this.colname=='')
			this.colname=this.name;
		this.colname=this.toUnderscore(this.colname);//this.colname.toLowerCase();
		this.dflt=this.findDefault(this.dflt,this.type,this.notnull);
	},
	
	findDefault:function(dflt,type,isnotnull)
	{
		if (!isnotnull)
			{return 'null';}
		if (type==='String')
		{
			if (dflt!=null)
				{return '"'+dflt+'"';}
			else {return '""';}
		}
		return 'null';
	},

	addValidators:function(maxlength,notnull,annotations)
	{
		if (isNaN(maxlength) && !notnull)
			{return;}
		if (!isNaN(maxlength))
			{annotations.push('@Length(max='+maxlength+')');}	       
		if (notnull==true)
			{annotations.push('@NotNull');}
	},
	
	getJavaBeanName:function(name)
	{
		var str=(name.substring(0,1)).toUpperCase();
		str+=name.substring(1);
		return str;
	},
	
	getPropertyAnnotations:function()
	{
		var annotations=[];
		if (this.json)
			annotations.push('@JsonProperty');
		this.addValidators(this.maxlength,this.notnull,annotations);
		//annotations.push('@Column(name="'+this.colname+'")');
		if (annotations.length==0)
			return '';
		else return annotations.join(' ')+'\n';
	},
	
	getMethodAnnotations:function()
	{
		return '';
	},
	
	createDeclaration:function()
	{
		var annotations=this.getPropertyAnnotations();
		var label=(this.label==null) ? '' : ' //'+this.label;
		return annotations+'protected '+this.type+' '+this.name+'='+this.dflt+';'+label+'\n';
	},
	
	createSetter:function()
	{
		var name=this.name;
		var str='\tpublic void set'+this.getJavaBeanName(name)+'(final '+this.type+' '+name+')';
		str+='{this.'+name+'='+name+';}\n';
		return str;
	},
	
	createGetter:function()
	{
		var name=this.name;
		var str='';
		str+=this.getMethodAnnotations();
		str+='\tpublic '+this.type+' get'+this.getJavaBeanName(name)+'()';
		str+='{return this.'+name+';}\n';
		return str;
	},

	createAccessors:function()
	{
		var str='';
		str+=this.createGetter();
		str+=this.createSetter();
		str+='\n';
		return str;
	},
	
	findSqlType:function(type,maxlength,notnull)
	{
		var nullable=(notnull) ? ' NOT NULL' : ' NULL';
		if (type==='Date')
			{return 'DATE'+nullable;}
		else if (type==='Boolean')
			{return 'BOOLEAN'+nullable;}
		else if (type==='Integer' || type==='Long')
			{return 'INTEGER'+nullable;}
		else if (type==='Float' || type==='Double')
			{return 'FLOAT'+nullable;}
		else if (type==='String' && isNaN(maxlength))
			{return 'TEXT'+nullable;}
		else if (type==='String')
		{
			if (maxlength!=null)
				{return 'VARCHAR('+parseInt(maxlength,10)+')'+nullable;}
			else {return 'TEXT'+nullable;}
		}
		else {throw 'Can\'t find mapping for SQL type '+type+' ('+this.name+')';}
	},
	
	createSql:function()
	{
		var sqltype=this.findSqlType(this.type,this.maxlength,this.notnull);
		var label=(this.label) ? ' --'+this.label : '';
		return '\t'+this.colname+' '+sqltype+','+label+'\n';
	},
	
	createSampleData:function()
	{
		return this.sample==null ? '' : 'obj.'+this.name+'='+this.sample+';\n';
	},
	
	createExtJsModelField:function()
	{
		return '{name: \''+this.name+'\' '+this.findJsType(this.type)+'},\n';
	},
	
	createExtJsGridField:function()
	{
		var attributes=[];
		attributes.push('dataIndex: '+this.quote(this.name));
		attributes.push('header: '+this.quote(this.label));
		if (this.type=='Date')
		{
			attributes.push('xtype: '+this.quote('datecolumn'));
			attributes.push('format: '+this.quote('Y-m-d'));
		}
		return '{'+attributes.join(', ')+'},\n'
	},
	
	findJsType:function(type)
	{
		if (type==='Date')
			{return ', type: \'date\', dateFormat: \'time\'';}
		else if (type==='Boolean')
			{return ', type: \'boolean\'';}
		else if (type==='Integer' || type==='Long')
			{return ', type: \'int\'';}
		else if (type==='Float' || type==='Double')
			{return ', type: \'float\'';}
		else if (type==='String')
			{return '';}
		else {throw 'Can\'t find mapping for JavaScript type '+type+' ('+this.name+')';}
	},
	
	quote:function(value)
	{
		return '\''+value+'\'';
	},
	
	toUnderscore: function(str)
	{
		return str.replace(/([A-Z])/g, function($1){return "_"+$1.toLowerCase();});
	}
	
	/*
	createExtJsFormField:function()
	{
		var funct=null;
		var params=[];
		params.push('name: \''+this.name+'\'');
		if (this.label)
			{params.push('fieldLabel: \''+this.label+'\'');}
		if (this.description)
			{params.push('helpText: \''+this.description+'\'');}
		switch (this.fieldtype)
		{
		case 'textfield':
			funct='createTextControl';
			break;
		case 'numberfield':
			funct='createNumberControl';
			break;
		case 'datefield':
			funct='createDateControl';
			break;
		case 'yesno':
			funct='createYesNoSelectList';
			break;
		case 'yesnomaybe':
			funct='createYesNoMaybeSelectList';
			break;
		case 'select':
			funct='createYesNoMaybeSelectList';
			break;
		}
		if (this.params)
			{params.push(this.params);}
		return '\t\t\t\tthis.'+funct+'({'+params.join(', ')+'}),\n';
	}
	*/
});

Ext.define('hiro.Dictionary', {

	constructor: function(str)
	{
		this.fields=[];
		var arr=str.split('\n');
		// use the first line to get the field names
		var names=this.getColumnNames(arr[0]);
		// create a config object mapping names and values
		for (var row=1;row<arr.length;row++) // start on the second line
		{
			var line=arr[row];
			if (line.trim()==='') // skip blank lines but don't trim content lines to maintain position
				{continue;}
			var values=line.split('\t');
			var config={};
			for (var col=0;col<values.length;col++)
			{
				var name=names[col];
				var value=values[col];
				config[name]=value;
			}
			this.fields.push(new hiro.Field(config));
		}
	},

	getColumnNames:function(line)
	{
		var names=[];
		var arr=line.split('\t');
		for (var i=0;i<arr.length;i++)
		{
			var name=arr[i];
			if (name==='field')
				{name='name';}
			else if (name==='length')
				{name='maxlength';}
			names.push(name);
		}
		return names;
	},
	
	createDeclarations:function()
	{
		var buffer=[], field;
		for (var i=0;i<this.fields.length;i++)
		{
			field=this.fields[i];
			buffer.push(field.createDeclaration());
		}
		return buffer.join('');
	},
	
	createAccessors:function()
	{
		var buffer=[], field;
		for (var i=0;i<this.fields.length;i++)
		{
			field=this.fields[i];
			buffer.push(field.createAccessors());
		}
		return buffer.join('');
	},
	
	createSql:function()
	{
		var buffer=[], field;
		for (var i=0;i<this.fields.length;i++)
		{
			field=this.fields[i];
			buffer.push(field.createSql());
		}
		return buffer.join('');
	},
	

	createSampleData:function()
	{
		var buffer=[], field;
		for (var i=0;i<this.fields.length;i++)
		{
			field=this.fields[i];
			buffer.push(field.createSampleData());
		}
		return buffer.join('');
	},
	
	createExtJsModelFields:function()
	{
		var buffer=[], field;
		for (var i=0;i<this.fields.length;i++)
		{
			field=this.fields[i];
			buffer.push(field.createExtJsModelField());
		}
		return buffer.join('');
	},
	
	createExtJsGridFields:function()
	{
		var buffer=[], field;
		for (var i=0;i<this.fields.length;i++)
		{
			field=this.fields[i];
			buffer.push(field.createExtJsGridField());
		}
		return buffer.join('');
	}
	
	/*
	
	
	createExtJsFormFields:function()
	{
		var buffer=[], field;
		for (var i=0;i<this.fields.length;i++)
		{
			field=this.fields[i];
			buffer.push(field.createExtJsFormField());
		}
		return buffer.join('');
	}
	*/
});