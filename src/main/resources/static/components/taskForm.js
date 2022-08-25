
Vue.component('task-form',{
  template: `<div class="card taskform" v-if="$store.task.id">
    <h5 class="card-title">{{$store.task.name}}</h5>
	<div id="task-form"></div>
	<div class="ms-2 me-2 mb-2 d-flex justify-content-between">
	  <button v-if="$store.task.assignee" type="button" class="btn btn-primary" @click="unclaim()">{{ $t("message.unclaim") }}</button>
	  <button v-if="!$store.task.assignee" type="button" class="btn btn-primary" @click="claim()">{{ $t("message.claim") }}</button>
	  <button :disabled="!$store.task.assignee || $store.task.assignee!=$store.user.name" type="button" class="btn btn-primary" @click="submit()">{{ $t("message.submit") }}</button>
	</div>
  </div>`,
  data() {
    return {
      form: null
    }
  },
  methods: {
    claim() {
      axios.get('/tasks/'+this.$store.task.id+'/claim/'+this.$store.user.name, this.$store.axiosHeaders).then(response => {
        this.$store.task.assignee=this.$store.user.name;
      }).catch(error => {
        alert(error.message);
      })
      this.form.setProperty('readOnly', false);
    },
    unclaim() {
      axios.get('/tasks/'+this.$store.task.id+'/unclaim/', this.$store.axiosHeaders).then(response => {
        this.$store.task.assignee=null;
      }).catch(error => {
        alert(error.message);
      })
      this.form.setProperty('readOnly', true);
    },
    submit() {
      this.form.validate();
      let errors = [];
      for (const field in this.form._state.errors) {
        if (this.form._state.errors[field].length>0) {
          Array.prototype.push.apply(errors, this.form._state.errors[field]);
        }
      }
      if (errors.length==0) {

        let taskId = this.$store.task.id;
        let formData = this.form._getState().data;

        this.form = null;

        let i =0;
        for(;i<this.$store.tasks.length;i++) {
          if (this.$store.tasks[i].id===this.$store.task.id) {
            this.$store.tasks.splice(i,1);
            break;
          }
        }

        axios.post('/tasks/'+taskId, formData, this.$store.axiosHeaders)
          .then(response => {
            /*  if(i<this.$store.tasks.length) {
              this.$store.task = this.$store.tasks[i];
            } else {
              this.$store.task = {
                id: null,
                name: null,
                creationTime: "1970-01-01"
              };
                this.form = null;
              }*/
          })
          .catch(error => {
            alert(error.message);
          })
      }
    },
    mountForm() {
      console.log("mounting form");
      if (!this.$store.task.id) {
        this.form = null;
      } else {

        // Dave's Note: Form schema is loaded from disk and sent as part of Task rather than
        // calling tasklist api
        //let url = '/forms/'+this.$store.task.processDefinitionId+'/'+this.$store.task.formKey;
        //axios.get(url, this.$store.axiosHeaders).then(response => {
        //  let schema = response.data;
        let schema = JSON.parse(this.$store.task.formSchema);
        //if (this.form==null) {
        // clear the old form
        let formEl = document.querySelector('#task-form');
        while(formEl.firstChild) {
          formEl.removeChild(formEl.firstChild);
        }
        this.form = new FormViewer.Form({
          container: formEl
        });
        //}
        this.form.importSchema(schema, this.$store.task.variables);
        if (!this.$store.task.assignee) {
          this.form.setProperty('readOnly', true);
        }
        //}).catch(error => {
          //alert(error.message);
        //})

      }
    }
  },
  updated() {
    this.mountForm();
  },
  mounted() {
    this.mountForm();
  }
});

