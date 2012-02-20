Ext.define('hiro.FormBuilder', {

	constructor: function(str)
	{
		this.fieldsets=[];
		var fieldset={rows: []};
		this.fieldsets.push(fieldset);
		var lines=str.split('\n');
		for (var i=0;i<lines.length;i++) // start on the second line
		{
			var line=lines[i];
			if (line.trim()=='') // skip blank lines but don't trim content lines to maintain position
			{
				fieldset={rows: []};
				this.fieldsets.push(fieldset);
				continue;
			}
			var tabs=line.split('\t');
			items=[];
			var config={name: '', flex: 0};
			items.push(config);
			for (var j=0;j<tabs.length;j++)
			{
				var tab=tabs[j];
				if (tab!=='')
				{
					config={name: tab, flex: 0};
					items.push(config);
				}
				//else if (config!=null)
				config.flex++;
			}
			fieldset.rows.push(items);
		}
		this.postProcess();
	},
	
	postProcess:function()
	{
		var fieldsets=[];
		for (var k=0; k<this.fieldsets.length; k++)
		{
			var fieldset=this.fieldsets[k];
			if (!fieldset.rows || fieldset.rows.length==0)
				continue;
			fieldsets.push(fieldset);
			for (var i=0;i<fieldset.rows.length;i++)
			{
				var items=fieldset.rows[i];
				fieldset.rows[i]=this.postProcessRow(items);
			}		
		}
		this.fieldsets=fieldsets;
	},
	
	postProcessRow:function(items)
	{
		var fields=[];
		for (var j=0;j<items.length;j++)
		{
			var item=items[j];
			if (item.flex==0)
				continue;
//			if (items[j+1] && items[j+1].value=='▼')
//			{
//				fields.push('\t\t\tthis.createSelectList({data: \''+item.name+'\', value: \''+item.name+'\', flex: '+(item.flex+1)+'})');
//				j++;
//			}
			fields.push('\t\t\tthis.createLabel({value: \''+item.name+'\', flex: '+item.flex+'})');
		}
		return fields;
	},

	createForm:function()
	{
		var buf=[];
		buf.push('this.items=');
		buf.push('[');
		for (var k=0; k<this.fieldsets.length; k++)
		{
			var fieldset=this.fieldsets[k];
			//if (fieldset.rows.length==0)
			//	continue;
//			var title=''; skiptitle=false;
//			if (fieldset.rows[0].length==1)
//			{
//				item=fieldset.rows[0][0];
//				console.log(item);
//				title=item;
//				skiptitle=true;
//			}
			buf.push('\tthis.createFieldset({title: \'todo\'},[');
			//var startindex= (skiptitle) ? 1 : 0
			for (var i=0;i<fieldset.rows.length;i++)
			{
				var items=fieldset.rows[i];
				buf.push('\t\tthis.createRow([');
				buf.push(items.join(',\n'));
//				for (var j=0;j<items.length;j++)
//				{
//					var item=items[j];
//					if (item.flex>0)
//					{
//						comma=j<items.length-1 ? ',' : '';
//						buf.push('\t\t\tthis.createLabel({value: \''+item.name+'\', flex: '+item.flex+'})'+comma);
//					}
//				}
				comma=i<fieldset.rows.length-1 ? ',' : '';
				buf.push('\t\t])'+comma);
			}			
			comma=k<this.fieldsets.length-1 ? ',' : '';
			buf.push('\t])'+comma);
		}
		buf.push('];');
		return buf.join('\n');
	}	
	
//	createForm:function()
//	{
//		var buf=[];
//		buf.push('this.items=');
//		buf.push('[');
//		for (var k=0; k<this.fieldsets.length; k++)
//		{
//			var fieldset=this.fieldsets[k];
//			if (fieldset.rows.length==0)
//				continue;
//			buf.push('\tthis.createFieldset({},[');
//			for (var i=0;i<fieldset.rows.length;i++)
//			{
//				var items=fieldset.rows[i];
//				buf.push('\t\tthis.createRow([');
//				for (var j=0;j<items.length;j++)
//				{
//					var item=items[j];
//					if (item.flex>0)
//					{
//						comma=j<items.length-1 ? ',' : '';
//						buf.push('\t\t\tthis.createLabel({value: \''+item.name+'\', flex: '+item.flex+'})'+comma);
//					}
//				}
//				comma=i<fieldset.rows.length-1 ? ',' : '';
//				buf.push('\t\t])'+comma);
//			}			
//			comma=k<this.fieldsets.length-1 ? ',' : '';
//			buf.push('\t])'+comma);
//		}
//		buf.push('];');
//		return buf.join('\n');
//	}
});

